package Modelo;

import java.util.Date;

public class Venta implements java.io.Serializable {
    private Date fecha;
    private double monto;
    
    private Entrada[] entradas;
    private int numEntradas;
    
    private Tarjeta tarjeta;

    private String dniCliente;
    private String nombreConcierto;
    private String nombreZona;

    private double montoSinDescuento;
    private double descuentoTarjeta;
    private double descuentoPuntos;
    private boolean usaPuntos;

    public Venta() {
        this.fecha = new Date();
        this.monto = 0.0;
        this.entradas = new Entrada[4];
        this.numEntradas = 0;
        this.tarjeta = null;
        this.dniCliente = "";
        this.nombreConcierto = "";
        this.nombreZona = "";
        this.montoSinDescuento = 0.0;
        this.descuentoTarjeta = 0.0;
        this.descuentoPuntos = 0.0;
        this.usaPuntos = false;
    }

    public boolean anular() {
        if (numEntradas == 0) {
            return false;
        }

        for (int i = 0; i < numEntradas; i++) {
            entradas[i].liberar();
            entradas[i] = null;
        }

        numEntradas = 0;
        monto = 0.0;

        return true;
    }

    public boolean agregarEntrada(Entrada e, double precio) {
        if (e == null) {
            throw new IllegalArgumentException("La entrada no puede ser nula.");
        }

        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }

        if (numEntradas >= 4) {
            throw new IllegalArgumentException("No puedes comprar más de 4 entradas por venta.");
        }

        for (int i = 0; i < numEntradas; i++) {
            if (entradas[i] == e) {
                throw new IllegalArgumentException("La entrada ya fue agregada a esta venta.");
            }
        }

        entradas[numEntradas] = e;
        numEntradas++;
        monto += precio;

        return true;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        if (fecha != null) {
            this.fecha = fecha;
        }
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        if (monto >= 0) {
            this.monto = monto;
        }
    }

    public Entrada[] getEntradas() {
        Entrada[] resultado = new Entrada[numEntradas];
        for (int i = 0; i < numEntradas; i++) {
            resultado[i] = entradas[i];
        }
        return resultado;
    }

    public int getCantidadEntradas() {
        return numEntradas;
    }

    public Tarjeta getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(Tarjeta tarjeta) {
        this.tarjeta = tarjeta;
    }

    public String getDniCliente() {
        return dniCliente;
    }

    public void setDniCliente(String dniCliente) {
        this.dniCliente = dniCliente;
    }

    public String getNombreConcierto() {
        return nombreConcierto;
    }

    public void setNombreConcierto(String nombreConcierto) {
        this.nombreConcierto = nombreConcierto;
    }

    public String getNombreZona() {
        return nombreZona;
    }

    public void setNombreZona(String nombreZona) {
        this.nombreZona = nombreZona;
    }

    public double getMontoSinDescuento() {
        return montoSinDescuento;
    }

    public void setMontoSinDescuento(double montoSinDescuento) {
        this.montoSinDescuento = montoSinDescuento;
    }

    public double getDescuentoTarjeta() {
        return descuentoTarjeta;
    }

    public void setDescuentoTarjeta(double descuentoTarjeta) {
        this.descuentoTarjeta = descuentoTarjeta;
    }

    public double getDescuentoPuntos() {
        return descuentoPuntos;
    }

    public void setDescuentoPuntos(double descuentoPuntos) {
        this.descuentoPuntos = descuentoPuntos;
    }

    public boolean isUsaPuntos() {
        return usaPuntos;
    }

    public void setUsaPuntos(boolean usaPuntos) {
        this.usaPuntos = usaPuntos;
    }

    public boolean devolver(int cantidad) {
        if (cantidad <= 0 || cantidad >= numEntradas) {
            return false;
        }

        double precioUnitario = monto / numEntradas;

        for (int i = 0; i < cantidad; i++) {
            if (numEntradas > 0) {
                entradas[numEntradas - 1].liberar();
                entradas[numEntradas - 1] = null;
                numEntradas--;
                monto -= precioUnitario;
            }
        }

        return true;
    }
}