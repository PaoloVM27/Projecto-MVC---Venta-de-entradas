package Modelo;

import Modelo.Cliente;
import Modelo.Tarjeta;
import Modelo.Venta;
import Persistencia.ArchivoCliente;

public class ArregloCliente {
    private Cliente clienteActual;
    private Cliente[] clientes;
    private int numClientes;
    private ArchivoCliente archivoCliente;

    public ArregloCliente() {
        this.clienteActual = null;
        this.clientes = null;
        this.numClientes = 0;
        this.archivoCliente = new ArchivoCliente();
    }

    public ArregloCliente(Cliente clienteActual) {
        this.clienteActual = clienteActual;
        this.clientes = null;
        this.numClientes = 0;
        this.archivoCliente = new ArchivoCliente();
    }

    public ArregloCliente(Cliente clienteActual, Cliente[] clientes, int numClientes) {
        this.clienteActual = clienteActual;
        this.clientes = clientes;
        this.numClientes = numClientes;
        this.archivoCliente = new ArchivoCliente();
    }

    public boolean asignarClienteActual(Cliente cliente) {
        if (cliente == null) {
            return false;
        }

        this.clienteActual = cliente;
        return true;
    }

    public boolean registrarTarjeta(long numero, String nombre, String fecha, double saldo) {
        if (clienteActual == null) {
            return false;
        }

        Tarjeta tarjeta = new Tarjeta(numero, nombre, fecha, saldo);

        boolean registrada = clienteActual.registrarTarjeta(tarjeta);

        if (registrada) {
            guardarCambios();
        }

        return registrada;
    }

    public boolean eliminarTarjeta() {
        if (clienteActual == null) {
            return false;
        }

        boolean eliminada = clienteActual.eliminarTarjeta();

        if (eliminada) {
            guardarCambios();
        }

        return eliminada;
    }

    private boolean guardarCambios() {
        if (clientes == null) {
            return true;
        }

        return archivoCliente.guardarClientes(clientes, numClientes);
    }

    public Tarjeta obtenerTarjeta() {
        if (clienteActual == null) {
            return null;
        }

        return clienteActual.getTarjeta();
    }

    public boolean tieneTarjeta() {
        if (clienteActual == null) {
            return false;
        }

        return clienteActual.getTarjeta() != null;
    }

    public int obtenerPuntos() {
        if (clienteActual == null) {
            return 0;
        }

        return clienteActual.getPuntos();
    }

    public String obtenerNombreCompleto() {
        if (clienteActual == null) {
            return "";
        }

        return clienteActual.getNombres() + " " + clienteActual.getApellidos();
    }

    public Cliente getClienteActual() {
        return clienteActual;
    }
}