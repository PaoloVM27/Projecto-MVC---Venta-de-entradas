package Controlador;

import Modelo.ArregloConcierto;
import Modelo.ArregloZonas;
import Modelo.Zona;
import Vista.VistaMenuAdmin;
import Vista.VistaCrearConcierto;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

public class ControladorCrearConcierto {
    private VistaCrearConcierto vista;
    private VistaMenuAdmin vistaMenuAdmin;
    private Servicios.Autenticacion auth;
    private ArregloConcierto arregloConcierto;
    private ArregloZonas arregloZonas;

    public ControladorCrearConcierto(VistaCrearConcierto vista, VistaMenuAdmin vistaMenuAdmin, Servicios.Autenticacion auth) {
        this.vista = vista;
        this.vistaMenuAdmin = vistaMenuAdmin;
        this.auth = auth;
        this.arregloConcierto = new ArregloConcierto();
        this.arregloZonas = new ArregloZonas();

        this.vista.btnCrear.addActionListener(e -> crearConcierto());
        this.vista.btnVolver.addActionListener(e -> volver());
    }

    public void iniciar() {
        cargarTablaZonas();

        javax.swing.JTextField txtNombre = obtenerTxtNombreZona();
        javax.swing.JTextField txtPrecio = obtenerTxtPrecioZona();
        javax.swing.JTextField txtCapacidad = obtenerTxtCapacidadZona();

        java.awt.event.ActionListener agregarAction = e -> agregarZonaTemporal();

        if (txtNombre != null) txtNombre.addActionListener(agregarAction);
        if (txtPrecio != null) txtPrecio.addActionListener(agregarAction);
        if (txtCapacidad != null) txtCapacidad.addActionListener(agregarAction);

        javax.swing.JButton btnAgregar = obtenerBtnAgregarZona();
        if (btnAgregar != null) {
            btnAgregar.addActionListener(agregarAction);
        }

        vista.setSize(700, 520);
        vista.setResizable(false);
        vista.setLocationRelativeTo(null);
        vista.setVisible(true);
    }

    private void agregarZonaTemporal() {
        javax.swing.JTextField txtNombre = obtenerTxtNombreZona();
        javax.swing.JTextField txtPrecio = obtenerTxtPrecioZona();
        javax.swing.JTextField txtCapacidad = obtenerTxtCapacidadZona();

        if (txtNombre == null || txtPrecio == null || txtCapacidad == null) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String capacidadStr = txtCapacidad.getText().trim();

        if (nombre.isEmpty() || precioStr.isEmpty() || capacidadStr.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor completa los tres campos de la zona.");
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);
            int capacidad = Integer.parseInt(capacidadStr);

            if (precio <= 0 || capacidad <= 0) {
                JOptionPane.showMessageDialog(vista, "Precio y capacidad deben ser valores positivos.");
                return;
            }

            boolean agregada = arregloZonas.agregarZona(nombre, precio, capacidad);

            if (!agregada) {
                JOptionPane.showMessageDialog(vista, "Ya existe una zona con ese nombre.");
                return;
            }

            cargarTablaZonas();

            txtNombre.setText("");
            txtPrecio.setText("");
            txtCapacidad.setText("");
            txtNombre.requestFocus();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vista, "Precio y capacidad deben ser numéricos.");
        }
    }

    private void cargarTablaZonas() {
        javax.swing.JTable tabla = obtenerTablaZonas();
        if (tabla == null) {
            return;
        }

        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
            new String[]{"Nombre", "Precio (S/)", "Capacidad"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Zona[] zonas = arregloZonas.obtenerZonas();
        if (zonas != null) {
            for (Zona z : zonas) {
                if (z != null) {
                    modelo.addRow(new Object[]{
                        z.getNombre(),
                        z.getPrecio(),
                        z.getCapacidad()
                    });
                }
            }
        }

        tabla.setModel(modelo);
    }

    private void crearConcierto() {
        String nombre = vista.txtNombreConcierto.getText().trim();
        String fechaTexto = vista.txtFechaConcierto.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Ingresa el nombre del concierto.");
            return;
        }

        if (fechaTexto.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Ingresa la fecha del concierto.");
            return;
        }

        if (arregloZonas.getNumZonas() == 0) {
            JOptionPane.showMessageDialog(vista, "Debes agregar al menos una zona para el concierto (escribe los datos a la derecha y presiona Enter).");
            return;
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            format.setLenient(false);
            Date fecha = format.parse(fechaTexto);

            double dVisa = 0, dMaster = 0, dAmex = 0, dDiners = 0;
            try {
                javax.swing.JTextField txtVisa = obtenerTxtDescuentoVisa();
                if (txtVisa != null && !txtVisa.getText().trim().isEmpty()) dVisa = Double.parseDouble(txtVisa.getText().trim());
                javax.swing.JTextField txtMaster = obtenerTxtDescuentoMasterCard();
                if (txtMaster != null && !txtMaster.getText().trim().isEmpty()) dMaster = Double.parseDouble(txtMaster.getText().trim());
                javax.swing.JTextField txtAmex = obtenerTxtDescuentoAmericanExpress();
                if (txtAmex != null && !txtAmex.getText().trim().isEmpty()) dAmex = Double.parseDouble(txtAmex.getText().trim());
                javax.swing.JTextField txtDiners = obtenerTxtDescuentoDinersClub();
                if (txtDiners != null && !txtDiners.getText().trim().isEmpty()) dDiners = Double.parseDouble(txtDiners.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(vista, "Los descuentos deben ser numericos.");
                return;
            }

            boolean creado = arregloConcierto.crearConcierto(nombre, fecha);

            if (creado) {
                Modelo.Usuario admin = auth.getUsuarioActual();
                if (admin != null) {
                    Zona[] listaZonas = arregloZonas.obtenerZonas();
                    for (Zona z : listaZonas) {
                        if (z != null) {
                            arregloConcierto.agregarZona(admin, nombre, z.getNombre(), z.getCapacidad(), z.getPrecio());
                        }
                    }
                }
                
                Modelo.Concierto c = arregloConcierto.buscarConcierto(nombre);
                if (c != null) {
                    c.setDescuentoVisa(dVisa);
                    c.setDescuentoMasterCard(dMaster);
                    c.setDescuentoAmericanExpress(dAmex);
                    c.setDescuentoDinersClub(dDiners);
                    arregloConcierto.guardarConciertos();
                }

                JOptionPane.showMessageDialog(vista, "Concierto creado con éxito.");
                vista.txtNombreConcierto.setText("");
                vista.txtFechaConcierto.setText("");
                try {
                    if (obtenerTxtDescuentoVisa() != null) obtenerTxtDescuentoVisa().setText("");
                    if (obtenerTxtDescuentoMasterCard() != null) obtenerTxtDescuentoMasterCard().setText("");
                    if (obtenerTxtDescuentoAmericanExpress() != null) obtenerTxtDescuentoAmericanExpress().setText("");
                    if (obtenerTxtDescuentoDinersClub() != null) obtenerTxtDescuentoDinersClub().setText("");
                } catch (Exception ex) {}
                arregloZonas.limpiar();
                cargarTablaZonas();
            } else {
                JOptionPane.showMessageDialog(vista, "No se pudo crear el concierto. Es posible que ya exista uno con el mismo nombre.");
            }

        } catch (ParseException e) {
            JOptionPane.showMessageDialog(vista, "Formato de fecha inválido. Usa el formato dd/MM/yyyy (ej: 25/12/2026).");
        }
    }

    private void volver() {
        vista.dispose();
        vistaMenuAdmin.setSize(700, 520);
        vistaMenuAdmin.setResizable(false);
        vistaMenuAdmin.setLocationRelativeTo(null);
        vistaMenuAdmin.setVisible(true);
    }

    private javax.swing.JTextField obtenerTxtNombreZona() {
        try {
            java.lang.reflect.Field field = vista.getClass().getDeclaredField("jTextField1");
            field.setAccessible(true);
            return (javax.swing.JTextField) field.get(vista);
        } catch (Exception ex) {
            return null;
        }
    }

    private javax.swing.JTextField obtenerTxtPrecioZona() {
        try {
            java.lang.reflect.Field field = vista.getClass().getDeclaredField("jTextField2");
            field.setAccessible(true);
            return (javax.swing.JTextField) field.get(vista);
        } catch (Exception ex) {
            return null;
        }
    }

    private javax.swing.JTextField obtenerTxtCapacidadZona() {
        try {
            java.lang.reflect.Field field = vista.getClass().getDeclaredField("jTextField3");
            field.setAccessible(true);
            return (javax.swing.JTextField) field.get(vista);
        } catch (Exception ex) {
            return null;
        }
    }

    private javax.swing.JTable obtenerTablaZonas() {
        try {
            java.lang.reflect.Field field = vista.getClass().getDeclaredField("jTable1");
            field.setAccessible(true);
            return (javax.swing.JTable) field.get(vista);
        } catch (Exception ex) {
            for (java.awt.Component comp : vista.getContentPane().getComponents()) {
                if (comp instanceof javax.swing.JScrollPane) {
                    javax.swing.JScrollPane sp = (javax.swing.JScrollPane) comp;
                    if (sp.getViewport().getView() instanceof javax.swing.JTable) {
                        return (javax.swing.JTable) sp.getViewport().getView();
                    }
                }
            }
            return null;
        }
    }

    private javax.swing.JButton obtenerBtnAgregarZona() {
        try {
            java.lang.reflect.Field field = vista.getClass().getDeclaredField("jButton1");
            field.setAccessible(true);
            return (javax.swing.JButton) field.get(vista);
        } catch (Exception ex) {
            for (java.awt.Component comp : vista.getContentPane().getComponents()) {
                if (comp instanceof javax.swing.JButton) {
                    javax.swing.JButton btn = (javax.swing.JButton) comp;
                    if (btn.getText() != null && btn.getText().toLowerCase().contains("agregar")) {
                        return btn;
                    }
                }
            }
            return null;
        }
    }

    private javax.swing.JTextField obtenerTxtDescuentoVisa() {
        try {
            java.lang.reflect.Field field = vista.getClass().getDeclaredField("jTextField4");
            field.setAccessible(true);
            return (javax.swing.JTextField) field.get(vista);
        } catch (Exception ex) { return null; }
    }

    private javax.swing.JTextField obtenerTxtDescuentoMasterCard() {
        try {
            java.lang.reflect.Field field = vista.getClass().getDeclaredField("jTextField5");
            field.setAccessible(true);
            return (javax.swing.JTextField) field.get(vista);
        } catch (Exception ex) { return null; }
    }

    private javax.swing.JTextField obtenerTxtDescuentoAmericanExpress() {
        try {
            java.lang.reflect.Field field = vista.getClass().getDeclaredField("jTextField6");
            field.setAccessible(true);
            return (javax.swing.JTextField) field.get(vista);
        } catch (Exception ex) { return null; }
    }

    private javax.swing.JTextField obtenerTxtDescuentoDinersClub() {
        try {
            java.lang.reflect.Field field = vista.getClass().getDeclaredField("jTextField7");
            field.setAccessible(true);
            return (javax.swing.JTextField) field.get(vista);
        } catch (Exception ex) { return null; }
    }
}
