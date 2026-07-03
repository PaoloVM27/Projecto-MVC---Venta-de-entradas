package Servicios;

import Modelo.Cliente;
import Modelo.Persona;
import Modelo.Usuario;
import Persistencia.ArchivoCliente;
import Persistencia.ArchivoUsuario;

public class Autenticacion {
    private static Persona[] personas;
    private static int numPersonas;
    private static boolean cargado = false;

    private Cliente clienteActual;
    private Usuario usuarioActual;
    private ArchivoCliente archivoCliente;
    private ArchivoUsuario archivoUsuario;

    public Autenticacion() {
        this.archivoCliente = new ArchivoCliente();
        this.archivoUsuario = new ArchivoUsuario();

        if (!cargado) {
            personas = new Persona[10];
            numPersonas = 0;

            Cliente[] clientesCargados = archivoCliente.cargarClientes();
            if (clientesCargados != null) {
                for (int i = 0; i < clientesCargados.length; i++) {
                    if (numPersonas >= personas.length) redimensionar();
                    personas[numPersonas++] = clientesCargados[i];
                }
            }

            Usuario[] usuariosCargados = archivoUsuario.cargarUsuarios();
            if (usuariosCargados != null) {
                for (int i = 0; i < usuariosCargados.length; i++) {
                    if (numPersonas >= personas.length) redimensionar();
                    personas[numPersonas++] = usuariosCargados[i];
                }
            }

            cargado = true;
        }

        this.clienteActual = null;
        this.usuarioActual = null;
    }

    private void redimensionar() {
        Persona[] nuevo = new Persona[personas.length * 2];
        for (int i = 0; i < numPersonas; i++) {
            nuevo[i] = personas[i];
        }
        personas = nuevo;
    }

    public boolean registrarCliente(String nombres, String apellidos, String dni, String correo, String contrasena) {
        if (nombres == null || nombres.isEmpty()) return false;
        if (apellidos == null || apellidos.isEmpty()) return false;
        if (dni == null || dni.isEmpty()) return false;
        if (correo == null || correo.isEmpty()) return false;
        if (contrasena == null || contrasena.isEmpty()) return false;
        if (buscarClientePorDni(dni) != null) return false;
        if (buscarUsuarioPorDni(dni) != null) return false;

        if (numPersonas >= personas.length) redimensionar();
        Cliente nuevoCliente = new Cliente(nombres, apellidos, dni, correo, contrasena);
        personas[numPersonas++] = nuevoCliente;

        boolean guardado = guardarClientes();
        if (!guardado) {
            personas[--numPersonas] = null;
            return false;
        }
        return true;
    }

    public boolean registrarUsuario(String nombres, String apellidos, String dni, String contrasena) {
        if (nombres == null || nombres.isEmpty()) return false;
        if (apellidos == null || apellidos.isEmpty()) return false;
        if (dni == null || dni.isEmpty()) return false;
        if (contrasena == null || contrasena.isEmpty()) return false;
        if (buscarUsuarioPorDni(dni) != null) return false;
        if (buscarClientePorDni(dni) != null) return false;

        if (numPersonas >= personas.length) redimensionar();
        Usuario nuevoUsuario = new Usuario(nombres, apellidos, dni, contrasena);
        personas[numPersonas++] = nuevoUsuario;

        boolean guardado = guardarUsuarios();
        if (!guardado) {
            personas[--numPersonas] = null;
            return false;
        }
        return true;
    }

    public boolean iniciarSesionCliente(String dni, String contrasena) {
        Cliente cliente = buscarClientePorDni(dni);
        if (cliente == null) return false;
        if (cliente.ingresar(dni, contrasena)) {
            clienteActual = cliente;
            usuarioActual = null;
            return true;
        }
        return false;
    }

    public boolean iniciarSesionUsuario(String dni, String contrasena) {
        Usuario usuario = buscarUsuarioPorDni(dni);
        if (usuario == null) return false;
        if (usuario.ingresar(dni, contrasena)) {
            usuarioActual = usuario;
            clienteActual = null;
            return true;
        }
        return false;
    }

    public void cerrarSesion() {
        clienteActual = null;
        usuarioActual = null;
    }

    public Cliente buscarClientePorDni(String dni) {
        for (int i = 0; i < numPersonas; i++) {
            if (personas[i] instanceof Cliente) {
                Cliente c = (Cliente) personas[i];
                if (c.getDni().equals(dni)) return c;
            }
        }
        return null;
    }

    public Usuario buscarUsuarioPorDni(String dni) {
        for (int i = 0; i < numPersonas; i++) {
            if (personas[i] instanceof Usuario) {
                Usuario u = (Usuario) personas[i];
                if (u.getDni().equals(dni)) return u;
            }
        }
        return null;
    }

    public boolean guardarClientes() {
        Cliente[] arr = getClientes();
        return archivoCliente.guardarClientes(arr, arr.length);
    }

    public boolean guardarUsuarios() {
        Usuario[] arr = getUsuarios();
        return archivoUsuario.guardarUsuarios(arr, arr.length);
    }

    public Cliente getClienteActual() {
        return clienteActual;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public Cliente[] getClientes() {
        int count = 0;
        for (int i = 0; i < numPersonas; i++) {
            if (personas[i] instanceof Cliente) count++;
        }
        Cliente[] result = new Cliente[count];
        int idx = 0;
        for (int i = 0; i < numPersonas; i++) {
            if (personas[i] instanceof Cliente) result[idx++] = (Cliente) personas[i];
        }
        return result;
    }

    public int getNumClientes() {
        int count = 0;
        for (int i = 0; i < numPersonas; i++) {
            if (personas[i] instanceof Cliente) count++;
        }
        return count;
    }

    public Usuario[] getUsuarios() {
        int count = 0;
        for (int i = 0; i < numPersonas; i++) {
            if (personas[i] instanceof Usuario) count++;
        }
        Usuario[] result = new Usuario[count];
        int idx = 0;
        for (int i = 0; i < numPersonas; i++) {
            if (personas[i] instanceof Usuario) result[idx++] = (Usuario) personas[i];
        }
        return result;
    }

    public int getNumUsuarios() {
        int count = 0;
        for (int i = 0; i < numPersonas; i++) {
            if (personas[i] instanceof Usuario) count++;
        }
        return count;
    }
}
