package Modelo;

import java.util.List;
import java.util.ArrayList;

public class Cliente extends Persona implements java.io.Serializable {
    private int puntos;
    private Tarjeta[] tarjetas;
    private int numTarjetas;
    private boolean estado;
    private String correo;

    public Cliente() {
        super();
        this.puntos = 0;
        this.tarjetas = new Tarjeta[10];
        this.numTarjetas = 0;
        this.estado = true;
        this.correo = "";
    }

    public Cliente(String nombres, String apellidos, String dni, String correo, String contrasena) {
        super(nombres, apellidos, dni, contrasena);
        this.puntos = 0;
        this.tarjetas = new Tarjeta[10];
        this.numTarjetas = 0;
        this.estado = true;
        this.correo = correo;
    }

    public boolean ingresar(String usuario, String clave) {
        return this.estado && this.dni.equals(usuario) && validarContrasena(clave);
    }

    public boolean activar() {
        this.estado = true;
        return true;
    }

    public boolean desactivar() {
        this.estado = false;
        return true;
    }

    public boolean isEstado() {
        return estado;
    }

    public boolean agregarTarjeta(Tarjeta tarjeta) {
        if (tarjeta == null) {
            return false;
        }

        if (!tarjeta.validarDatos()) {
            return false;
        }

        for (int i = 0; i < numTarjetas; i++) {
            if (tarjetas[i] != null && tarjetas[i].getNumero() == tarjeta.getNumero()) {
                return false;
            }
        }

        if (numTarjetas >= tarjetas.length) {
            redimensionarTarjetas();
        }

        this.tarjetas[numTarjetas++] = tarjeta;
        return true;
    }

    private void redimensionarTarjetas() {
        Tarjeta[] nuevo = new Tarjeta[tarjetas.length * 2];
        for (int i = 0; i < numTarjetas; i++) {
            nuevo[i] = tarjetas[i];
        }
        tarjetas = nuevo;
    }

    public boolean actualizarTarjeta(int indice, Tarjeta nuevaTarjeta) {
        if (indice < 0 || indice >= numTarjetas || nuevaTarjeta == null) {
            return false;
        }
        if (!nuevaTarjeta.validarDatos()) {
            return false;
        }
        for (int i = 0; i < numTarjetas; i++) {
            if (i != indice && tarjetas[i] != null && tarjetas[i].getNumero() == nuevaTarjeta.getNumero()) {
                return false;
            }
        }
        this.tarjetas[indice] = nuevaTarjeta;
        return true;
    }

    public boolean registrarTarjeta(Tarjeta tarjeta) {
        return agregarTarjeta(tarjeta);
    }

    @Override
    public boolean registrarTarjeta() {
        return this.numTarjetas > 0;
    }

    public boolean eliminarTarjeta() {
        if (this.numTarjetas == 0) {
            return false;
        }
        this.tarjetas[--this.numTarjetas] = null;
        return true;
    }

    public boolean eliminarTarjeta(long numero) {
        int indice = -1;
        for (int i = 0; i < numTarjetas; i++) {
            if (tarjetas[i] != null && tarjetas[i].getNumero() == numero) {
                indice = i;
                break;
            }
        }

        if (indice == -1) {
            return false;
        }

        for (int i = indice; i < numTarjetas - 1; i++) {
            tarjetas[i] = tarjetas[i + 1];
        }
        tarjetas[--numTarjetas] = null;
        return true;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        if (puntos < 0) {
            this.puntos = 0;
        } else {
            this.puntos = puntos;
        }
    }

    public Tarjeta getTarjeta() {
        return numTarjetas > 0 ? tarjetas[0] : null;
    }

    public Tarjeta[] getTarjetas() {
        Tarjeta[] activas = new Tarjeta[numTarjetas];
        for (int i = 0; i < numTarjetas; i++) {
            activas[i] = tarjetas[i];
        }
        return activas;
    }

    public int getNumTarjetas() {
        return numTarjetas;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}