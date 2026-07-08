package Modelo;

import Modelo.Cliente;
import Modelo.Tarjeta;

public class ArregloTarjetas {
    private Cliente clienteActual;
    private Cliente[] clientes;
    private int numClientes;

    public ArregloTarjetas(Cliente clienteActual, Cliente[] clientes, int numClientes) {
        this.clienteActual = clienteActual;
        this.clientes = clientes;
        this.numClientes = numClientes;
    }

    public boolean registrarTarjeta(long numero, String nombre, String fecha, double saldo) {
        if (clienteActual == null) {
            return false;
        }

        Tarjeta tarjeta = new Tarjeta(numero, nombre, fecha, saldo);
        boolean registrada = clienteActual.agregarTarjeta(tarjeta);

        if (registrada) {
            guardarCambios();
        }

        return registrada;
    }

    public boolean actualizarTarjeta(int indice, long numero, String nombre, String fecha, double saldo) {
        if (clienteActual == null) {
            return false;
        }

        Tarjeta tarjeta = new Tarjeta(numero, nombre, fecha, saldo);
        boolean actualizada = clienteActual.actualizarTarjeta(indice, tarjeta);

        if (actualizada) {
            guardarCambios();
        }

        return actualizada;
    }

    public boolean eliminarTarjeta(long numero) {
        if (clienteActual == null) {
            return false;
        }

        boolean eliminada = clienteActual.eliminarTarjeta(numero);

        if (eliminada) {
            guardarCambios();
        }

        return eliminada;
    }

    public Tarjeta[] listarTarjetas() {
        if (clienteActual == null) {
            return new Tarjeta[0];
        }
        return clienteActual.getTarjetas();
    }

    private boolean guardarCambios() {
        if (clientes == null) {
            return true;
        }
        Persistencia.ArchivoCliente archivoCliente = new Persistencia.ArchivoCliente();
        return archivoCliente.guardarClientes(clientes, numClientes);
    }
}
