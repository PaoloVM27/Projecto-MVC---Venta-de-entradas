package Controlador;

import Modelo.Cliente;
import Modelo.Concierto;
import Modelo.Tarjeta;
import Modelo.Venta;
import Modelo.Zona;
import Vista.VistaCompra;
import Vista.VistaMenuCliente;

public class ControladorCompra {
    private VistaCompra vistaCompra;
    private VistaMenuCliente vistaMenuCliente;
    private Servicios.Autenticacion auth;

    private Modelo.ArregloConcierto arregloConcierto;
    private Modelo.ArregloVentas arregloVentas;
    private Modelo.ArregloCliente arregloCliente;

    private Concierto concierto;

    public ControladorCompra(VistaCompra vistaCompra, VistaMenuCliente vistaMenuCliente, Servicios.Autenticacion auth) {
        this.vistaCompra = vistaCompra;
        this.vistaMenuCliente = vistaMenuCliente;
        this.auth = auth;

        this.arregloConcierto = new Modelo.ArregloConcierto();
        this.arregloVentas = new Modelo.ArregloVentas(auth.getClientes(), auth.getNumClientes());
        this.arregloCliente = new Modelo.ArregloCliente(auth.getClienteActual(), auth.getClientes(), auth.getNumClientes());

        this.vistaCompra.btnComprar.addActionListener(e -> comprarEntradas());
        this.vistaCompra.btnVolver.addActionListener(e -> volverMenu());
        this.vistaCompra.cboZona.addActionListener(e -> calcularMonto());
        this.vistaCompra.spnCantidad.addChangeListener(e -> calcularMonto());
    }

    public void iniciar() {
        Concierto[] listaConciertos = arregloConcierto.listarConciertos();
        
        vistaCompra.cboConcierto.removeAllItems();
        for (Concierto c : listaConciertos) {
            if (c != null) {
                vistaCompra.cboConcierto.addItem(c.getNombre());
            }
        }

        vistaCompra.cboConcierto.addActionListener(e -> actualizarConciertoSeleccionado());

        arregloVentas.cargarVentas(listaConciertos, listaConciertos.length);

        cargarTarjetaCliente();

        actualizarConciertoSeleccionado();
        
        vistaCompra.setSize(700, 520);
        vistaCompra.setResizable(false);
        vistaCompra.setLocationRelativeTo(null);
        vistaCompra.setVisible(true);
    }

    private void actualizarConciertoSeleccionado() {
        Object seleccionado = vistaCompra.cboConcierto.getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        String nombreConcierto = seleccionado.toString();
        concierto = arregloConcierto.buscarConcierto(nombreConcierto);

        if (concierto != null) {
            vistaCompra.cboZona.removeAllItems();
            for (Zona z : concierto.getZonas()) {
                if (z != null) {
                    vistaCompra.cboZona.addItem(z.getNombre());
                }
            }
            calcularMonto();
        }
    }

    private void cargarTarjetaCliente() {
        Cliente cliente = auth.getClienteActual();

        if (cliente == null) {
            javax.swing.JOptionPane.showMessageDialog(vistaCompra, "No hay cliente logueado.");
            return;
        }

        javax.swing.JComboBox<String> combo = obtenerComboBoxTarjetas();
        if (combo != null) {
            for (java.awt.event.ActionListener al : combo.getActionListeners()) {
                combo.removeActionListener(al);
            }
            combo.removeAllItems();

            Tarjeta[] tarjetas = cliente.getTarjetas();
            if (tarjetas != null && tarjetas.length > 0) {
                for (Tarjeta t : tarjetas) {
                    if (t != null) {
                        String numStr = String.valueOf(t.getNumero());
                        combo.addItem(obtenerTipoTarjeta(numStr) + " - " + enmascararNumero(numStr));
                    }
                }
                combo.addActionListener(e -> seleccionarTarjetaCombo());
                combo.setSelectedIndex(0);
                seleccionarTarjetaCombo();
            } else {
                vistaCompra.txtNumeroTarjeta.setText("");
                vistaCompra.txtNombreTarjeta.setText("");
                vistaCompra.txtResumen.setText("No tienes tarjetas registradas.\nRegistra una tarjeta en Método de Pago.");
            }
        }

        vistaCompra.txtNumeroTarjeta.setEditable(false);
        vistaCompra.txtNombreTarjeta.setEditable(false);
    }

    private void seleccionarTarjetaCombo() {
        javax.swing.JComboBox<String> combo = obtenerComboBoxTarjetas();
        if (combo == null) return;
        int idx = combo.getSelectedIndex();
        Cliente cliente = auth.getClienteActual();
        if (cliente != null && idx != -1) {
            Tarjeta[] tarjetas = cliente.getTarjetas();
            if (tarjetas != null && idx < tarjetas.length) {
                Tarjeta t = tarjetas[idx];
                if (t != null) {
                    vistaCompra.txtNumeroTarjeta.setText(enmascararNumero(String.valueOf(t.getNumero())));
                    vistaCompra.txtNombreTarjeta.setText(t.getNombre());
                }
            }
        }
        calcularMonto();
    }

    private String enmascararNumero(String numStr) {
        if (numStr.length() > 4) {
            int asteriscos = numStr.length() - 4;
            String res = "";
            for (int i = 0; i < asteriscos; i++) {
                res += "*";
            }
            return res + numStr.substring(asteriscos);
        }
        return numStr;
    }

    private String obtenerTipoTarjeta(String numStr) {
        if (numStr.startsWith("4") && numStr.length() == 16) {
            return "Visa";
        } else if ((numStr.startsWith("5") || numStr.startsWith("2")) && numStr.length() == 16) {
            return "Mastercard";
        } else if (numStr.startsWith("3") && numStr.length() == 15) {
            return "American Express";
        } else if (numStr.startsWith("3") && numStr.length() == 14) {
            return "Diners Club";
        }
        return "Tarjeta";
    }

    private Zona obtenerZonaSeleccionada() {
        Object seleccionado = vistaCompra.cboZona.getSelectedItem();
        if (seleccionado == null) {
            return null;
        }

        String nombreZona = seleccionado.toString();

        if (concierto == null) {
            return null;
        }

        return concierto.buscarZona(nombreZona);
    }

    private void calcularMonto() {
        Zona zona = obtenerZonaSeleccionada();

        if (zona == null) {
            return;
        }

        int cantidad = Integer.parseInt(vistaCompra.spnCantidad.getValue().toString());
        
        double descuento = 0.0;
        try {
            javax.swing.JComboBox<String> combo = obtenerComboBoxTarjetas();
            if (combo != null && combo.getSelectedIndex() != -1) {
                String tarjetaTexto = combo.getSelectedItem().toString().toLowerCase();
                if (tarjetaTexto.contains("visa")) descuento = concierto.getDescuentoVisa();
                else if (tarjetaTexto.contains("master")) descuento = concierto.getDescuentoMasterCard();
                else if (tarjetaTexto.contains("american") || tarjetaTexto.contains("express")) descuento = concierto.getDescuentoAmericanExpress();
                else if (tarjetaTexto.contains("diners")) descuento = concierto.getDescuentoDinersClub();
            }
        } catch (Exception e) {}
        
        double descuentoPuntos = 0.0;
        Cliente cliente = auth.getClienteActual();
        if (cliente != null && cliente.getPuntos() >= 10) {
            descuentoPuntos = 5.0;
        }
        
        double descuentoTotal = descuento + descuentoPuntos;
        
        double originalPrecio = zona.getPrecio();
        double precioDescuento = originalPrecio - (originalPrecio * (descuentoTotal / 100.0));
        double monto = precioDescuento * cantidad;
        
        javax.swing.JTextField txtDesc = obtenerTxtDescuentoCompra();
        if (txtDesc != null) {
            txtDesc.setText((descuentoTotal % 1 == 0 ? String.valueOf((int)descuentoTotal) : String.valueOf(descuentoTotal)));
            txtDesc.setEditable(false);
        }

        vistaCompra.lblMonto.setText("Monto: S/ " + monto);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String resumen = "";
        resumen += "Concierto: " + concierto.getNombre() + "\n";
        resumen += "Fecha: " + sdf.format(concierto.getFecha()) + "\n";
        resumen += "Zona seleccionada: " + zona.getNombre() + "\n";
        resumen += "Precio original: S/ " + originalPrecio + "\n";
        if (descuentoPuntos > 0) {
            resumen += "Descuento por tarjeta: " + (descuento % 1 == 0 ? (int)descuento : descuento) + "%\n";
            resumen += "Descuento por puntos (10 pts): " + (descuentoPuntos % 1 == 0 ? (int)descuentoPuntos : descuentoPuntos) + "%\n";
            resumen += "Descuento total: " + (descuentoTotal % 1 == 0 ? (int)descuentoTotal : descuentoTotal) + "%\n";
        } else {
            resumen += "Descuento: " + (descuentoTotal % 1 == 0 ? (int)descuentoTotal : descuentoTotal) + "%\n";
        }
        resumen += "Precio por entrada: S/ " + precioDescuento + "\n";
        resumen += "Cantidad: " + cantidad + "\n";
        resumen += "Monto total: S/ " + monto + "\n";
        resumen += "Entradas disponibles: " + zona.contarDisponibles() + "\n";

        vistaCompra.txtResumen.setText(resumen);
    }

    private void comprarEntradas() {
        try {
            Cliente cliente = auth.getClienteActual();

            if (cliente == null) {
                javax.swing.JOptionPane.showMessageDialog(vistaCompra, "No hay cliente logueado.");
                return;
            }

            Zona zona = obtenerZonaSeleccionada();

            if (zona == null) {
                javax.swing.JOptionPane.showMessageDialog(vistaCompra, "Zona no encontrada.");
                return;
            }

            javax.swing.JComboBox<String> combo = obtenerComboBoxTarjetas();
            if (combo == null || combo.getSelectedIndex() == -1) {
                javax.swing.JOptionPane.showMessageDialog(vistaCompra, "No tienes una tarjeta seleccionada para el pago.");
                return;
            }

            int idx = combo.getSelectedIndex();
            Tarjeta[] tarjetasClie = cliente.getTarjetas();
            if (tarjetasClie == null || idx >= tarjetasClie.length) {
                javax.swing.JOptionPane.showMessageDialog(vistaCompra, "Error al seleccionar la tarjeta.");
                return;
            }

            try {
                java.lang.reflect.Field field = cliente.getClass().getDeclaredField("tarjetas");
                field.setAccessible(true);
                Tarjeta[] orig = (Tarjeta[]) field.get(cliente);
                if (orig != null && idx > 0 && idx < orig.length) {
                    Tarjeta temp = orig[0];
                    orig[0] = orig[idx];
                    orig[idx] = temp;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            int cantidad = Integer.parseInt(vistaCompra.spnCantidad.getValue().toString());

            double descuento = 0.0;
            try {
                if (combo != null && combo.getSelectedIndex() != -1) {
                    String tarjetaTexto = combo.getSelectedItem().toString().toLowerCase();
                    if (tarjetaTexto.contains("visa")) descuento = concierto.getDescuentoVisa();
                    else if (tarjetaTexto.contains("master")) descuento = concierto.getDescuentoMasterCard();
                    else if (tarjetaTexto.contains("american") || tarjetaTexto.contains("express")) descuento = concierto.getDescuentoAmericanExpress();
                    else if (tarjetaTexto.contains("diners")) descuento = concierto.getDescuentoDinersClub();
                }
            } catch (Exception e) {}
            
            double descuentoPuntos = 0.0;
            if (cliente != null && cliente.getPuntos() >= 10) {
                descuentoPuntos = 5.0;
            }
            
            double descuentoTotal = descuento + descuentoPuntos;
            
            double originalPrecio = zona.getPrecio();
            double precioDescuento = originalPrecio - (originalPrecio * (descuentoTotal / 100.0));
            
            try {
                java.lang.reflect.Field fieldPrecio = zona.getClass().getDeclaredField("precio");
                fieldPrecio.setAccessible(true);
                fieldPrecio.set(zona, precioDescuento);
            } catch (Exception ex) {}

            Venta venta = arregloVentas.comprarEntradas(
                    cliente,
                    concierto,
                    zona,
                    cantidad
            );
            
            try {
                java.lang.reflect.Field fieldPrecio = zona.getClass().getDeclaredField("precio");
                fieldPrecio.setAccessible(true);
                fieldPrecio.set(zona, originalPrecio);
            } catch (Exception ex) {}

            arregloConcierto.guardarConciertos();
            
            if (descuentoPuntos > 0) {
                cliente.setPuntos(cliente.getPuntos() - 10);
                try {
                    java.lang.reflect.Method mGuardar = arregloCliente.getClass().getDeclaredMethod("guardarCambios");
                    mGuardar.setAccessible(true);
                    mGuardar.invoke(arregloCliente);
                } catch (Exception ex) {}
            }

            Tarjeta tarjeta = cliente.getTarjeta();

            vistaCompra.txtNumeroTarjeta.setText(enmascararNumero(String.valueOf(tarjeta.getNumero())));
            vistaCompra.txtNombreTarjeta.setText(tarjeta.getNombre());

            vistaCompra.txtNumeroTarjeta.setEditable(false);
            vistaCompra.txtNombreTarjeta.setEditable(false);

            String resumen = "";
            resumen += "COMPRA REALIZADA CORRECTAMENTE\n";
            resumen += "Cliente: " + cliente.getNombres() + " " + cliente.getApellidos() + "\n";
            resumen += "Concierto: " + venta.getNombreConcierto() + "\n";
            resumen += "Zona: " + venta.getNombreZona() + "\n";
            resumen += "Cantidad de entradas: " + venta.getCantidadEntradas() + "\n";
            if (descuentoPuntos > 0) {
                resumen += "Descuento por tarjeta: " + (descuento % 1 == 0 ? (int)descuento : descuento) + "%\n";
                resumen += "Descuento por puntos (10 pts): " + (descuentoPuntos % 1 == 0 ? (int)descuentoPuntos : descuentoPuntos) + "%\n";
                resumen += "Descuento total aplicado: " + (descuentoTotal % 1 == 0 ? (int)descuentoTotal : descuentoTotal) + "%\n";
            } else {
                resumen += "Descuento aplicado: " + (descuentoTotal % 1 == 0 ? (int)descuentoTotal : descuentoTotal) + "%\n";
            }
            resumen += "Precio original por entrada: S/ " + originalPrecio + "\n";
            resumen += "Precio final por entrada: S/ " + precioDescuento + "\n";
            resumen += "Monto total sin descuento: S/ " + (originalPrecio * cantidad) + "\n";
            resumen += "Cobro total final: S/ " + venta.getMonto() + "\n";
            resumen += "Entradas disponibles en zona: " + zona.contarDisponibles() + "\n";

            vistaCompra.txtResumen.setText(resumen);
            vistaCompra.lblMonto.setText("Monto: S/ " + venta.getMonto());

            javax.swing.JOptionPane.showMessageDialog(vistaCompra, "Compra realizada correctamente.");

        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(vistaCompra, "Revisa los datos numéricos de la tarjeta.");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(vistaCompra, e.getMessage());
        }
    }

    private void volverMenu() {
        vistaCompra.dispose();

        vistaMenuCliente.setSize(700, 520);
        vistaMenuCliente.setResizable(false);
        vistaMenuCliente.setLocationRelativeTo(null);
        vistaMenuCliente.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private javax.swing.JComboBox<String> obtenerComboBoxTarjetas() {
        try {
            java.lang.reflect.Field field = vistaCompra.getClass().getDeclaredField("jComboBox1");
            field.setAccessible(true);
            return (javax.swing.JComboBox<String>) field.get(vistaCompra);
        } catch (Exception ex) {
            for (java.awt.Component comp : vistaCompra.getContentPane().getComponents()) {
                if (comp instanceof javax.swing.JComboBox) {
                    javax.swing.JComboBox<String> combo = (javax.swing.JComboBox<String>) comp;
                    if (combo != vistaCompra.cboConcierto && combo != vistaCompra.cboZona) {
                        return combo;
                    }
                }
            }
        }
        return null;
    }

    private javax.swing.JTextField obtenerTxtDescuentoCompra() {
        try {
            java.awt.Component[] components = vistaCompra.getContentPane().getComponents();
            javax.swing.JLabel targetLabel = null;
            for (java.awt.Component comp : components) {
                if (comp instanceof javax.swing.JLabel) {
                    javax.swing.JLabel lbl = (javax.swing.JLabel) comp;
                    if (lbl.getText() != null && lbl.getText().toLowerCase().contains("descuento")) {
                        targetLabel = lbl;
                        break;
                    }
                }
            }
            if (targetLabel != null) {
                javax.swing.JTextField closestText = null;
                int minDistance = Integer.MAX_VALUE;
                for (java.awt.Component comp : components) {
                    if (comp instanceof javax.swing.JTextField) {
                        int diffY = Math.abs(comp.getY() - targetLabel.getY());
                        if (diffY < 20 && comp.getX() > targetLabel.getX()) {
                            int dist = comp.getX() - targetLabel.getX();
                            if (dist < minDistance) {
                                minDistance = dist;
                                closestText = (javax.swing.JTextField) comp;
                            }
                        }
                    }
                }
                return closestText;
            }
        } catch (Exception ex) {}
        
        return null;
    }
}