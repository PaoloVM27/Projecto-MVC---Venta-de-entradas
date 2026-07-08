package Modelo;

import Modelo.Cliente;
import Modelo.Concierto;
import Modelo.Entrada;
import Modelo.Tarjeta;
import Modelo.Venta;
import Modelo.Zona;

import Persistencia.ArchivoCliente;
import Persistencia.ArchivoVenta;

public class ArregloVentas {
    private Venta[] ventas;
    private int numVentas;
    
    private Cliente[] clientes;
    private int numClientes;

    private ArchivoVenta archivoVenta;
    private ArchivoCliente archivoCliente;

    public ArregloVentas() {
        this.ventas = new Venta[10];
        this.numVentas = 0;
        this.clientes = null;
        this.numClientes = 0;
        this.archivoVenta = new ArchivoVenta();
        this.archivoCliente = new ArchivoCliente();
    }

    public ArregloVentas(Cliente[] clientes, int numClientes) {
        this.ventas = new Venta[10];
        this.numVentas = 0;
        this.clientes = clientes;
        this.numClientes = numClientes;
        this.archivoVenta = new ArchivoVenta();
        this.archivoCliente = new ArchivoCliente();
    }
    
    private void redimensionarVentas() {
        Venta[] nuevoArreglo = new Venta[ventas.length * 2];
        for (int i = 0; i < numVentas; i++) {
            nuevoArreglo[i] = ventas[i];
        }
        ventas = nuevoArreglo;
    }

    public Venta comprarEntradas(Cliente cliente, Concierto concierto, Zona zona, int cantidad, boolean usaPuntos, double descuentoTarjeta, double descuentoPuntos, double montoSinDescuento) {
        if (cliente == null || concierto == null || zona == null || cantidad <= 0) {
            return null;
        }

        if (concierto == null) {
            throw new IllegalArgumentException("El concierto no puede ser nulo.");
        }

        if (cantidad > 4) {
            throw new IllegalArgumentException("Solo puedes comprar hasta 4 entradas por venta.");
        }

        Tarjeta tarjeta = cliente.getTarjeta();

        if (tarjeta == null) {
            throw new IllegalStateException("El cliente no tiene una tarjeta registrada.");
        }

        Entrada[] entradasVendidas = zona.venderEntrada(cantidad);

        Venta venta = new Venta();
        venta.setTarjeta(tarjeta);
        venta.setDniCliente(cliente.getDni());
        venta.setNombreConcierto(concierto.getNombre());
        venta.setNombreZona(zona.getNombre());
        venta.setTarjeta(cliente.getTarjeta());
        venta.setUsaPuntos(usaPuntos);
        venta.setDescuentoTarjeta(descuentoTarjeta);
        venta.setDescuentoPuntos(descuentoPuntos);
        venta.setMontoSinDescuento(montoSinDescuento);

        boolean ventaConcierto = concierto.agregarVenta(venta);
        for (int i = 0; i < entradasVendidas.length; i++) {
            if (entradasVendidas[i] != null) {
                venta.agregarEntrada(entradasVendidas[i], zona.getPrecio());
            }
        }

        boolean pagoCorrecto = tarjeta.procesarCobro(venta.getMonto());

        if (!pagoCorrecto) {
            venta.anular();
            throw new IllegalStateException("No se pudo procesar el pago.");
        }

        concierto.agregarVenta(venta);
        
        if (numVentas >= ventas.length) {
            redimensionarVentas();
        }
        ventas[numVentas] = venta;
        numVentas++;

        if (!usaPuntos) {
            cliente.setPuntos(cliente.getPuntos() + venta.getCantidadEntradas());
        }

        guardarCambios();

        return venta;
    }

    public Venta comprarEntradas(Cliente cliente, Zona zona, int cantidad) {
        throw new IllegalArgumentException("Para persistencia debes enviar también el concierto.");
    }

    public boolean anularVenta(Concierto concierto, Cliente cliente, Venta venta) {
        if (concierto == null) {
            return false;
        }

        if (cliente == null) {
            return false;
        }

        if (venta == null) {
            return false;
        }

        int puntosARestar = venta.getCantidadEntradas();
        boolean anulada = concierto.anularVenta(venta);

        if (anulada) {
            for (int i = 0; i < numVentas; i++) {
                if (ventas[i] != null && ventas[i].getDniCliente().equals(venta.getDniCliente()) && ventas[i].getFecha().getTime() == venta.getFecha().getTime()) {
                    for (int j = i; j < numVentas - 1; j++) {
                        ventas[j] = ventas[j + 1];
                    }
                    ventas[numVentas - 1] = null;
                    numVentas--;
                    break;
                }
            }
            
            int nuevosPuntos = cliente.getPuntos() - puntosARestar;
            cliente.setPuntos(nuevosPuntos < 0 ? 0 : nuevosPuntos);
            guardarCambios();
        }

        return anulada;
    }

    public boolean devolverEntradas(Concierto concierto, Cliente cliente, Venta venta, int cantidadADevolver) {
        if (concierto == null || cliente == null || venta == null) {
            return false;
        }

        if (cantidadADevolver <= 0 || cantidadADevolver > venta.getCantidadEntradas()) {
            return false;
        }

        if (cantidadADevolver == venta.getCantidadEntradas()) {
            return anularVenta(concierto, cliente, venta);
        }

        boolean devuelto = venta.devolver(cantidadADevolver);
        if (devuelto) {
            int nuevosPuntos = cliente.getPuntos() - cantidadADevolver;
            cliente.setPuntos(nuevosPuntos < 0 ? 0 : nuevosPuntos);
            guardarCambios();
            return true;
        }

        return false;
    }

    public boolean cargarVentas(Concierto[] conciertos, int numConciertos) {
        if (clientes == null) {
            return false;
        }

        Venta[] cargadas = archivoVenta.cargarVentas(clientes, numClientes, conciertos, numConciertos);
        if (cargadas != null) {
            ventas = new Venta[Math.max(10, cargadas.length * 2)];
            numVentas = 0;
            for (int i = 0; i < cargadas.length; i++) {
                if (cargadas[i] != null) {
                    ventas[numVentas] = cargadas[i];
                    numVentas++;
                }
            }
        }
        return true;
    }

    private boolean guardarCambios() {
        if (clientes == null) {
            return true;
        }

        boolean clientesGuardados = archivoCliente.guardarClientes(clientes, numClientes);
        boolean ventasGuardadas = archivoVenta.guardarVentas(ventas, numVentas);

        return clientesGuardados && ventasGuardadas;
    }

    public Venta[] listarVentas() {
        Venta[] resultado = new Venta[numVentas];
        for (int i = 0; i < numVentas; i++) {
            resultado[i] = ventas[i];
        }
        return resultado;
    }

    public double calcularMonto(Zona zona, int cantidad) {
        if (zona == null) {
            return 0.0;
        }

        if (cantidad <= 0) {
            return 0.0;
        }

        if (cantidad > 4) {
            return 0.0;
        }

        return zona.getPrecio() * cantidad;
    }
}