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
        
        this.vistaMetodoPago.txtNumeroTarjeta.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarIconoTarjeta(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarIconoTarjeta(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarIconoTarjeta(); }
        });
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

                String numStr = String.valueOf(t.getNumero());
                actualizarIconoTarjeta();
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

            if (numeroTexto.isEmpty() || nombre.isEmpty() || fecha.isEmpty()) {
                mostrarMensaje("Completa todos los datos de la tarjeta.");
                return;
            }

            if (!numeroTexto.matches("\\d+")) {
                mostrarMensaje("El número de tarjeta debe contener solo dígitos.");
                return;
            }

            String tipo = "";
            if (numeroTexto.startsWith("4")) {
                tipo = "Visa";
            } else if (numeroTexto.startsWith("5") || numeroTexto.startsWith("2")) {
                tipo = "Mastercard";
            } else if (numeroTexto.startsWith("3")) {
                if (numeroTexto.length() == 15) tipo = "American Express";
                else if (numeroTexto.length() == 14) tipo = "Diners Club";
                else tipo = "American Express / Diners Club";
            }

            boolean esValida = false;
            String recordatorio = "";

            if (tipo.equalsIgnoreCase("Visa")) {
                if (numeroTexto.length() == 16) {
                    esValida = true;
                } else {
                    recordatorio = "Recordatorio para Visa:\n- Debe tener exactamente 16 dígitos.";
                }
            } else if (tipo.equalsIgnoreCase("Mastercard")) {
                if (numeroTexto.length() == 16) {
                    esValida = true;
                } else {
                    recordatorio = "Recordatorio para Mastercard:\n- Debe tener exactamente 16 dígitos.";
                }
            } else if (tipo.equalsIgnoreCase("American Express")) {
                if (numeroTexto.length() == 15) {
                    esValida = true;
                } else {
                    recordatorio = "Recordatorio para American Express:\n- Debe tener exactamente 15 dígitos.";
                }
            } else if (tipo.equalsIgnoreCase("Diners Club")) {
                if (numeroTexto.length() == 14) {
                    esValida = true;
                } else {
                    recordatorio = "Recordatorio para Diners Club:\n- Debe tener exactamente 14 dígitos.";
                }
            } else {
                recordatorio = "El número ingresado no coincide con ningún tipo de tarjeta soportado (Visa, Mastercard, Amex, Diners).";
            }

            if (!esValida) {
                throw new Exception("Número de tarjeta inválido.\n\n" + recordatorio);
            }

            long numero = Long.parseLong(numeroTexto);
            double saldo = 0.0;

            boolean operacionExitosa;
            String mensajeExito;

            if (this.filaSeleccionada != -1) {
                operacionExitosa = arregloTarjetas.actualizarTarjeta(
                        this.filaSeleccionada,
                        numero,
                        nombre,
                        fecha,
                        saldo
                );
                mensajeExito = "Tarjeta actualizada correctamente.";
            } else {
                operacionExitosa = arregloTarjetas.registrarTarjeta(
                        numero,
                        nombre,
                        fecha,
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
            mostrarMensaje("Número de tarjeta debe ser un valor numérico.");
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

    private javax.swing.JLabel obtenerLabelIcono() {
        try {
            java.lang.reflect.Field field = vistaMetodoPago.getClass().getDeclaredField("lblIconoTarjeta");
            field.setAccessible(true);
            return (javax.swing.JLabel) field.get(vistaMetodoPago);
        } catch (Exception ex) {
            return null;
        }
    }

    private void actualizarIconoTarjeta() {
        javax.swing.JLabel lblIcono = obtenerLabelIcono();
        if (lblIcono == null) return;
        
        String numero = vistaMetodoPago.txtNumeroTarjeta.getText().trim();
        String rutaBase = "/imagenes/";
        String archivoImg = "";
        
        if (numero.startsWith("4")) {
            archivoImg = "visa.png";
        } else if (numero.startsWith("5") || numero.startsWith("2")) {
            archivoImg = "mastercard.png";
        } else if (numero.startsWith("3")) {
            if (numero.startsWith("34") || numero.startsWith("37")) {
                archivoImg = "amex.png";
            } else {
                archivoImg = "diners.png";
            }
        }
        
        if (!archivoImg.isEmpty()) {
            java.net.URL imgUrl = getClass().getResource("/iconos/" + archivoImg);
            
            if (imgUrl == null) {
                imgUrl = getClass().getResource("/" + archivoImg);
            }
            
            if (imgUrl != null) {
                javax.swing.ImageIcon iconoOriginal = new javax.swing.ImageIcon(imgUrl);
                java.awt.Image imagenOriginal = iconoOriginal.getImage();
                
                int ancho = lblIcono.getWidth();
                int alto = lblIcono.getHeight();
                
                if (ancho <= 0) ancho = 60;
                if (alto <= 0) alto = 40;
                
                java.awt.Image imagenEscalada = imagenOriginal.getScaledInstance(ancho, alto, java.awt.Image.SCALE_SMOOTH);
                
                lblIcono.setIcon(new javax.swing.ImageIcon(imagenEscalada));
                lblIcono.setText("");
            } else {
                lblIcono.setIcon(null);
                lblIcono.setText(archivoImg.replace(".png", "").toUpperCase());
            }
        } else {
            lblIcono.setIcon(null);
            lblIcono.setText("?");
        }
    }
}