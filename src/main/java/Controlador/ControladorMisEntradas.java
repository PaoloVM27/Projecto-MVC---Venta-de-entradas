package Controlador;

import Modelo.ArregloConcierto;
import Modelo.ArregloVentas;
import Modelo.Cliente;
import Modelo.Concierto;
import Modelo.Entrada;
import Modelo.Venta;
import Servicios.Autenticacion;
import Vista.VistaMenuCliente;
import Vista.VistaMisEntradas;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ControladorMisEntradas {
    private VistaMisEntradas vistaMisEntradas;
    private VistaMenuCliente vistaMenu;
    private Autenticacion auth;

    private ArregloConcierto arregloConcierto;
    private ArregloVentas arregloVentas;

    public ControladorMisEntradas(VistaMisEntradas vistaMisEntradas, VistaMenuCliente vistaMenu, Autenticacion auth) {
        this.vistaMisEntradas = vistaMisEntradas;
        this.vistaMenu = vistaMenu;
        this.auth = auth;

        this.arregloConcierto = new ArregloConcierto();
        this.arregloVentas = new ArregloVentas(auth.getClientes(), auth.getNumClientes());

        this.vistaMisEntradas.btnActualizar.addActionListener(e -> cargarEntradas());
        this.vistaMisEntradas.btnVolver.addActionListener(e -> volverMenu());
    }

    public void iniciar() {
        prepararTabla();
        cargarEntradas();

        vistaMisEntradas.setSize(700, 520);
        vistaMisEntradas.setResizable(false);
        vistaMisEntradas.setLocationRelativeTo(null);

        javax.swing.JButton btnDev = null;
        for (java.awt.Component c : vistaMisEntradas.getContentPane().getComponents()) {
            if (c instanceof javax.swing.JButton) {
                if (((javax.swing.JButton) c).getText().toLowerCase().contains("devoluci")) {
                    btnDev = (javax.swing.JButton) c;
                    break;
                }
            }
        }
        if (btnDev == null) {
            btnDev = new javax.swing.JButton("Solicitar Devolución");
            vistaMisEntradas.getContentPane().add(btnDev, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 360, 160, 30));
        }
        
        for (java.awt.event.ActionListener al : btnDev.getActionListeners()) {
            btnDev.removeActionListener(al);
        }
        btnDev.addActionListener(e -> solicitarDevolucion());

        vistaMisEntradas.setVisible(true);
    }

    private void prepararTabla() {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        modelo.addColumn("Fecha");
        modelo.addColumn("Concierto");
        modelo.addColumn("Zona");
        modelo.addColumn("Cantidad");
        modelo.addColumn("Puntos");
        modelo.addColumn("Monto Total");
        modelo.addColumn("Desc. Tarjeta");
        modelo.addColumn("Desc. Puntos");
        modelo.addColumn("Monto Final");

        vistaMisEntradas.tblEntradas.setModel(modelo);
    }

    private void cargarEntradas() {
        Cliente cliente = auth.getClienteActual();

        if (cliente == null) {
            mostrarMensaje("No hay cliente logueado.");
            volverMenu();
            return;
        }

        Concierto[] conciertos = arregloConcierto.listarConciertos();
        arregloVentas.cargarVentas(conciertos, conciertos.length);

        Venta[] ventas = arregloVentas.listarVentas();

        DefaultTableModel modelo = (DefaultTableModel) vistaMisEntradas.tblEntradas.getModel();
        modelo.setRowCount(0);

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

        double totalGastado = 0.0;
        int cantidadCompras = 0;

        for (int i = 0; i < ventas.length; i++) {
            Venta venta = ventas[i];

            if (venta != null && cliente.getDni().equals(venta.getDniCliente())) {
                Object[] fila = new Object[9];

                fila[0] = formatoFecha.format(venta.getFecha());
                fila[1] = venta.getNombreConcierto();
                fila[2] = venta.getNombreZona();
                fila[3] = venta.getCantidadEntradas();
                
                if (venta.isUsaPuntos()) {
                    fila[4] = "-";
                } else {
                    fila[4] = "+" + venta.getCantidadEntradas();
                }

                fila[5] = "S/ " + String.format("%.2f", venta.getMontoSinDescuento());
                
                if (venta.getDescuentoTarjeta() > 0) {
                    fila[6] = (venta.getDescuentoTarjeta() % 1 == 0 ? (int)venta.getDescuentoTarjeta() : venta.getDescuentoTarjeta()) + "%";
                } else {
                    fila[6] = "-";
                }
                
                if (venta.getDescuentoPuntos() > 0) {
                    fila[7] = (venta.getDescuentoPuntos() % 1 == 0 ? (int)venta.getDescuentoPuntos() : venta.getDescuentoPuntos()) + "%";
                } else {
                    fila[7] = "-";
                }

                fila[8] = "S/ " + String.format("%.2f", venta.getMonto());

                modelo.addRow(fila);

                totalGastado += venta.getMonto();
                cantidadCompras++;
            }
        }

        vistaMisEntradas.lblCantidadCompras.setText("Cantidad de compras: " + cantidadCompras);
        vistaMisEntradas.lblTotalGastado.setText("Total gastado: S/ " + String.format("%.2f", totalGastado));
    }

    private String obtenerNumerosEntradas(Venta venta) {
        Entrada[] entradas = venta.getEntradas();
        String texto = "";

        for (int i = 0; i < entradas.length; i++) {
            texto += entradas[i].getNumero();

            if (i < entradas.length - 1) {
                texto += ", ";
            }
        }

        return texto;
    }

    private void volverMenu() {
        vistaMisEntradas.dispose();

        vistaMenu.setSize(700, 520);
        vistaMenu.setResizable(false);
        vistaMenu.setLocationRelativeTo(null);
        vistaMenu.setVisible(true);
    }

    private void solicitarDevolucion() {
        int filaSel = vistaMisEntradas.tblEntradas.getSelectedRow();
        if (filaSel == -1) {
            mostrarMensaje("Seleccione una compra de la tabla para solicitar la devolución.");
            return;
        }

        Cliente cliente = auth.getClienteActual();
        if (cliente == null) return;

        Venta ventaSelec = null;
        int contador = 0;
        for (Venta v : arregloVentas.listarVentas()) {
            if (v != null && v.getDniCliente().equals(cliente.getDni())) {
                if (contador == filaSel) {
                    ventaSelec = v;
                    break;
                }
                contador++;
            }
        }

        if (ventaSelec == null) {
            mostrarMensaje("No se pudo encontrar la información de la compra.");
            return;
        }

        Concierto concierto = arregloConcierto.buscarConcierto(ventaSelec.getNombreConcierto());
        if (concierto == null) return;

        long difMillis = concierto.getFecha().getTime() - new java.util.Date().getTime();
        long horasDif = difMillis / (1000 * 60 * 60);

        if (horasDif < 168) {
            mostrarMensaje("No se permite la devolución.\nRegla: Deben faltar al menos 7 días (168 horas) para el evento.");
            return;
        }

        int maxDevolver = ventaSelec.getCantidadEntradas();
        String input = JOptionPane.showInputDialog(vistaMisEntradas, 
            "Regla: Se permite devolución total o parcial si se solicita con un mínimo de 7 días (168 horas) de anticipación.\n\n" +
            "¿Cuántas entradas deseas devolver de esta compra? (Máximo " + maxDevolver + ")");

        if (input != null && !input.trim().isEmpty()) {
            try {
                int cant = Integer.parseInt(input.trim());
                if (cant <= 0 || cant > maxDevolver) {
                    mostrarMensaje("Cantidad inválida. Debe ser entre 1 y " + maxDevolver + ".");
                    return;
                }
                
                double comision = 0.0;
                if (!ventaSelec.isUsaPuntos() && cliente.getPuntos() < cant) {
                    int puntosFaltantes = cant - cliente.getPuntos();
                    comision = puntosFaltantes * 5.00;
                    
                    int respuesta = JOptionPane.showConfirmDialog(vistaMisEntradas, 
                        "Ya gastaste los puntos obtenidos por esta compra y te faltan " + puntosFaltantes + " puntos para poder devolver " + cant + " entradas.\n" +
                        "Se te cobrará una comisión de S/ " + comision + " que será restada de tu reembolso final.\n\n" +
                        "¿Deseas continuar con la devolución?", 
                        "Comisión por puntos faltantes", JOptionPane.YES_NO_OPTION);
                        
                    if (respuesta != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                boolean exito = arregloVentas.devolverEntradas(concierto, cliente, ventaSelec, cant);
                if (exito) {
                    arregloConcierto.guardarConciertos();
                    double precioPorEntrada = ventaSelec.getMonto() / ventaSelec.getCantidadEntradas();
                    double montoReembolso = (precioPorEntrada * cant) - comision;
                    
                    if (montoReembolso < 0) montoReembolso = 0;
                    
                    String mensajeExito = "Devolución procesada correctamente.\nSe ha reembolsado S/ " + String.format("%.2f", montoReembolso) + " a tu tarjeta.";
                    if (comision > 0) {
                        mensajeExito += "\n(Se descontó S/ " + String.format("%.2f", comision) + " por comisión de puntos)";
                    }
                    
                    mostrarMensaje(mensajeExito);
                    cargarEntradas();
                } else {
                    mostrarMensaje("Error al procesar la devolución.");
                }
            } catch (NumberFormatException ex) {
                mostrarMensaje("Debe ingresar un número válido.");
            }
        }
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(vistaMisEntradas, mensaje);
    }
}