package Controlador;

import Modelo.ArregloTarjetas;
import Modelo.Cliente;
import Modelo.Tarjeta;
import Servicios.Autenticacion;
import Vista.VistaMenuCliente;
import Vista.VistaMetodoPago;
import javax.swing.JOptionPane;

public class ControladorMetodoPago {
    private VistaMetodoPago vistaMetodoPago;
    private VistaMenuCliente vistaMenu;
    private Autenticacion auth;
    private ArregloTarjetas arregloTarjetas;
    private int filaSeleccionada = -1;

    public ControladorMetodoPago(VistaMetodoPago vistaMetodoPago, VistaMenuCliente vistaMenu, Autenticacion auth) {
        this.vistaMetodoPago = vistaMetodoPago;
        this.vistaMenu = vistaMenu;
        this.auth = auth;

        this.arregloTarjetas = new ArregloTarjetas(
                auth.getClienteActual(),
                auth.getClientes(),
                auth.getNumClientes()
        );

        this.vistaMetodoPago.btnGuardarTarjeta.addActionListener(e -> guardarTarjeta());
        this.vistaMetodoPago.btnEliminarTarjeta.addActionListener(e -> eliminarTarjeta());
        this.vistaMetodoPago.btnVolver.addActionListener(e -> volverMenu());
    }

    public void iniciar() {
        cargarTabla();

        javax.swing.JTable tabla = obtenerTabla();
        if (tabla != null) {
            tabla.getSelectionModel().addListSelectionListener(listEvent -> {
                if (!listEvent.getValueIsAdjusting()) {
                    tarjetaSeleccionadaModificada();
                }
            });

            tabla.addMouseListener(new java.awt.event.MouseAdapter() {
                private int lastSelectedRow = -2;

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    int row = tabla.rowAtPoint(e.getPoint());
                    if (row == -1) {
                        tabla.clearSelection();
                        lastSelectedRow = -2;
                    } else {
                        if (row == lastSelectedRow) {
                            tabla.clearSelection();
                            lastSelectedRow = -2;
                        } else {
                            lastSelectedRow = row;
                        }
                    }
                }
            });
        }

        vistaMetodoPago.setSize(700, 520);
        vistaMetodoPago.setResizable(false);
        vistaMetodoPago.setLocationRelativeTo(null);
        vistaMetodoPago.setVisible(true);
    }

    private void tarjetaSeleccionadaModificada() {
        javax.swing.JTable tabla = obtenerTabla();
        if (tabla == null) return;

        int fila = tabla.getSelectedRow();
        this.filaSeleccionada = fila;

        if (fila == -1) {
            limpiarCampos();
            return;
        }

        Tarjeta[] tarjetas = arregloTarjetas.listarTarjetas();
        if (tarjetas != null && fila < tarjetas.length) {
            Tarjeta t = tarjetas[fila];
            if (t != null) {
                vistaMetodoPago.txtNumeroTarjeta.setText(String.valueOf(t.getNumero()));
                vistaMetodoPago.txtNombreTarjeta.setText(t.getNombre());
                vistaMetodoPago.txtFechaTarjeta.setText(t.getFecha());
                vistaMetodoPago.txtCvv.setText(String.valueOf(t.getCvv()));

                String numStr = String.valueOf(t.getNumero());
                javax.swing.JComboBox<String> combo = obtenerComboBox();
                if (combo != null) {
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        String item = combo.getItemAt(i);
                        if (item.equalsIgnoreCase("Visa") && numStr.startsWith("4")) {
                            combo.setSelectedIndex(i);
                            break;
                        } else if (item.equalsIgnoreCase("Mastercard") && (numStr.startsWith("5") || numStr.startsWith("2"))) {
                            combo.setSelectedIndex(i);
                            break;
                        } else if (item.equalsIgnoreCase("American Express") && numStr.startsWith("3") && numStr.length() == 15) {
                            combo.setSelectedIndex(i);
                            break;
                        } else if (item.equalsIgnoreCase("Diners Club") && numStr.startsWith("3") && numStr.length() == 14) {
                            combo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void cargarTabla() {
        javax.swing.JTable tabla = obtenerTabla();
        if (tabla == null) {
            return;
        }

        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
            new String[]{"Tipo", "Número", "Titular", "Vencimiento"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Tarjeta[] tarjetas = arregloTarjetas.listarTarjetas();
        if (tarjetas != null) {
            for (Tarjeta t : tarjetas) {
                if (t != null) {
                    String numStr = String.valueOf(t.getNumero());
                    String numOculto = "";
                    if (numStr.length() > 4) {
                        int asteriscos = numStr.length() - 4;
                        for (int i = 0; i < asteriscos; i++) {
                            numOculto += "*";
                        }
                        numOculto += numStr.substring(asteriscos);
                    } else {
                        numOculto = numStr;
                    }

                    String tipo = "Desconocido";
                    if (numStr.startsWith("4") && numStr.length() == 16) {
                        tipo = "Visa";
                    } else if ((numStr.startsWith("5") || numStr.startsWith("2")) && numStr.length() == 16) {
                        tipo = "Mastercard";
                    } else if (numStr.startsWith("3") && numStr.length() == 15) {
                        tipo = "American Express";
                    } else if (numStr.startsWith("3") && numStr.length() == 14) {
                        tipo = "Diners Club";
                    }

                    modelo.addRow(new Object[]{
                        tipo,
                        numOculto,
                        t.getNombre(),
                        t.getFecha()
                    });
                }
            }
        }

        tabla.setModel(modelo);
    }

    private void guardarTarjeta() {
        try {
            Cliente cliente = auth.getClienteActual();

            if (cliente == null) {
                mostrarMensaje("No hay cliente logueado.");
                return;
            }

            String numeroTexto = vistaMetodoPago.txtNumeroTarjeta.getText().trim();
            String nombre = vistaMetodoPago.txtNombreTarjeta.getText().trim();
            String fecha = vistaMetodoPago.txtFechaTarjeta.getText().trim();
            String cvvTexto = vistaMetodoPago.txtCvv.getText().trim();

            if (numeroTexto.isEmpty() || nombre.isEmpty() || fecha.isEmpty()
                    || cvvTexto.isEmpty()) {
                mostrarMensaje("Completa todos los datos de la tarjeta.");
                return;
            }

            if (!numeroTexto.matches("\\d+")) {
                mostrarMensaje("El número de tarjeta debe contener solo dígitos.");
                return;
            }
            if (!cvvTexto.matches("\\d+")) {
                mostrarMensaje("El CVV/CID debe contener solo dígitos.");
                return;
            }

            javax.swing.JComboBox<String> combo = obtenerComboBox();
            String tipo = (combo != null && combo.getSelectedItem() != null) ? combo.getSelectedItem().toString() : "";
            boolean esValida = false;
            String recordatorio = "";

            if (tipo.equalsIgnoreCase("Visa")) {
                if (numeroTexto.startsWith("4") && numeroTexto.length() == 16 && cvvTexto.length() == 3) {
                    esValida = true;
                } else {
                    recordatorio = "Recordatorio de especificaciones para Visa:\n"
                            + "- El número debe comenzar con 4.\n"
                            + "- Debe tener exactamente 16 dígitos.\n"
                            + "- El código de seguridad (CVV) debe tener exactamente 3 dígitos.";
                }
            } else if (tipo.equalsIgnoreCase("Mastercard")) {
                if ((numeroTexto.startsWith("5") || numeroTexto.startsWith("2")) && numeroTexto.length() == 16 && cvvTexto.length() == 3) {
                    esValida = true;
                } else {
                    recordatorio = "Recordatorio de especificaciones para Mastercard:\n"
                            + "- El número debe comenzar con 5 o 2.\n"
                            + "- Debe tener exactamente 16 dígitos.\n"
                            + "- El código de seguridad (CVV) debe tener exactamente 3 dígitos.";
                }
            } else if (tipo.equalsIgnoreCase("American Express")) {
                if (numeroTexto.startsWith("3") && numeroTexto.length() == 15 && cvvTexto.length() == 4) {
                    esValida = true;
                } else {
                    recordatorio = "Recordatorio de especificaciones para American Express:\n"
                            + "- El número debe comenzar con 3.\n"
                            + "- Debe tener exactamente 15 dígitos.\n"
                            + "- El código de seguridad (CID) debe tener exactamente 4 dígitos.";
                }
            } else if (tipo.equalsIgnoreCase("Diners Club")) {
                if (numeroTexto.startsWith("3") && numeroTexto.length() == 14 && cvvTexto.length() == 3) {
                    esValida = true;
                } else {
                    recordatorio = "Recordatorio de especificaciones para Diners Club:\n"
                            + "- El número debe comenzar con 3.\n"
                            + "- Debe tener exactamente 14 dígitos.\n"
                            + "- El código de seguridad (CVV) debe tener exactamente 3 dígitos.";
                }
            }

            if (!esValida) {
                throw new Exception("Datos de tarjeta inválidos.\n\n" + recordatorio);
            }

            long numero = Long.parseLong(numeroTexto);
            int cvv = Integer.parseInt(cvvTexto);
            double saldo = 0.0;

            boolean operacionExitosa;
            String mensajeExito;

            if (this.filaSeleccionada != -1) {
                operacionExitosa = arregloTarjetas.actualizarTarjeta(
                        this.filaSeleccionada,
                        numero,
                        nombre,
                        fecha,
                        cvv,
                        saldo
                );
                mensajeExito = "Tarjeta actualizada correctamente.";
            } else {
                operacionExitosa = arregloTarjetas.registrarTarjeta(
                        numero,
                        nombre,
                        fecha,
                        cvv,
                        saldo
                );
                mensajeExito = "Tarjeta guardada correctamente.";
            }

            if (!operacionExitosa) {
                mostrarMensaje("No se pudo procesar la tarjeta. Revisa los datos o puede que ya esté registrada.");
                return;
            }

            mostrarMensaje(mensajeExito);
            
            javax.swing.JTable tabla = obtenerTabla();
            if (tabla != null) {
                tabla.clearSelection();
            }
            limpiarCampos();
            cargarTabla();

        } catch (NumberFormatException e) {
            mostrarMensaje("Número de tarjeta y CVV deben ser valores numéricos.");
        } catch (Exception e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void eliminarTarjeta() {
        javax.swing.JTable tabla = obtenerTabla();
        if (tabla == null) {
            return;
        }

        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            mostrarMensaje("Selecciona una tarjeta de la tabla para eliminar.");
            return;
        }

        Tarjeta[] tarjetas = arregloTarjetas.listarTarjetas();
        if (tarjetas == null || fila >= tarjetas.length) {
            return;
        }

        Tarjeta tarjetaAEliminar = tarjetas[fila];
        if (tarjetaAEliminar == null) {
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(
                vistaMetodoPago,
                "¿Seguro que deseas eliminar la tarjeta seleccionada?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminada = arregloTarjetas.eliminarTarjeta(tarjetaAEliminar.getNumero());

        if (!eliminada) {
            mostrarMensaje("No se pudo eliminar la tarjeta.");
            return;
        }

        limpiarCampos();
        cargarTabla();
        mostrarMensaje("Tarjeta eliminada correctamente.");
    }

    private void limpiarCampos() {
        vistaMetodoPago.txtNumeroTarjeta.setText("");
        vistaMetodoPago.txtNombreTarjeta.setText("");
        vistaMetodoPago.txtFechaTarjeta.setText("");
        vistaMetodoPago.txtCvv.setText("");
    }

    private void volverMenu() {
        vistaMetodoPago.dispose();

        vistaMenu.setSize(700, 520);
        vistaMenu.setResizable(false);
        vistaMenu.setLocationRelativeTo(null);
        vistaMenu.setVisible(true);
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(vistaMetodoPago, mensaje);
    }

    private javax.swing.JTable obtenerTabla() {
        try {
            java.lang.reflect.Field field = vistaMetodoPago.getClass().getDeclaredField("jTable1");
            field.setAccessible(true);
            return (javax.swing.JTable) field.get(vistaMetodoPago);
        } catch (Exception ex) {
            for (java.awt.Component comp : vistaMetodoPago.getContentPane().getComponents()) {
                if (comp instanceof javax.swing.JScrollPane) {
                    javax.swing.JScrollPane sp = (javax.swing.JScrollPane) comp;
                    if (sp.getViewport().getView() instanceof javax.swing.JTable) {
                        return (javax.swing.JTable) sp.getViewport().getView();
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private javax.swing.JComboBox<String> obtenerComboBox() {
        try {
            java.lang.reflect.Field field = vistaMetodoPago.getClass().getDeclaredField("jComboBox1");
            field.setAccessible(true);
            return (javax.swing.JComboBox<String>) field.get(vistaMetodoPago);
        } catch (Exception ex) {
            for (java.awt.Component comp : vistaMetodoPago.getContentPane().getComponents()) {
                if (comp instanceof javax.swing.JComboBox) {
                    return (javax.swing.JComboBox<String>) comp;
                }
            }
        }
        return null;
    }
}