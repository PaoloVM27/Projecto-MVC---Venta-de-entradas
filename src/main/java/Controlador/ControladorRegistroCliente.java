package Controlador;

import Vista.VistaLogin;
import Vista.VistaRegistroCliente;

public class ControladorRegistroCliente {
    private VistaRegistroCliente vistaRegistro;
    private VistaLogin vistaLogin;
    private Servicios.Autenticacion auth;

    public ControladorRegistroCliente(VistaRegistroCliente vistaRegistro, VistaLogin vistaLogin, Servicios.Autenticacion auth) {
        this.vistaRegistro = vistaRegistro;
        this.vistaLogin = vistaLogin;
        this.auth = auth;

        this.vistaRegistro.btnRegistrar.addActionListener(e -> registrarCliente());
        this.vistaRegistro.btnVolver.addActionListener(e -> volverLogin());
        this.vistaRegistro.jCheckBox1.addActionListener(e -> toggleContrasena());
    }

    public void iniciar() {
        vistaRegistro.setSize(700, 520);
        vistaRegistro.setResizable(false);
        vistaRegistro.setLocationRelativeTo(null);
        vistaRegistro.setVisible(true);
    }

    private void registrarCliente() {
        String correo = "";
        javax.swing.JTextField txtCorreo = obtenerTxtCorreo();
        if (txtCorreo != null) {
            correo = txtCorreo.getText().trim();
        }
        
        String nombres = vistaRegistro.txtNombres.getText().trim();
        String apellidos = vistaRegistro.txtApellidos.getText().trim();
        String dni = vistaRegistro.txtDni.getText().trim();
        String contrasena = new String(vistaRegistro.txtContrasena.getPassword()).trim();
        String confirmarContrasena = new String(vistaRegistro.txtConfirmarContrasena.getPassword()).trim();

        if (nombres.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Ingresa tus nombres.");
            return;
        }

        if (apellidos.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Ingresa tus apellidos.");
            return;
        }

        if (dni.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Ingresa tu DNI.");
            return;
        }

        String fechaNacText = vistaRegistro.jTextField1.getText().trim();
        if (fechaNacText.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Ingresa tu fecha de nacimiento.");
            return;
        }

        java.time.LocalDate fechaNac;
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            fechaNac = java.time.LocalDate.parse(fechaNacText, formatter);
        } catch (java.time.format.DateTimeParseException e) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "El formato de fecha de nacimiento debe ser dd/mm/aaaa.");
            return;
        }

        java.time.LocalDate ahora = java.time.LocalDate.now();
        int edad = java.time.Period.between(fechaNac, ahora).getYears();
        
        try {
            if (edad < 18) {
                throw new Exception("Debes ser mayor de edad (18 años o más) para registrarte.");
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, e.getMessage());
            return;
        }
        
        if (correo.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Ingresa un correo electrónico.");
            return;
        }

        if (contrasena.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Ingresa una contraseña.");
            return;
        }

        if (confirmarContrasena.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Confirma tu contraseña.");
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Las contraseñas no coinciden.");
            return;
        }

        java.util.Random rnd = new java.util.Random();
        String codigoGenerado = String.format("%04d", rnd.nextInt(10000));
        
        try {
            java.util.Properties props = new java.util.Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            
            javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication("sistemaentradasunmsm@gmail.com", "qisbehwhtfegqeaq");
                }
            });
            
            javax.mail.Message message = new javax.mail.internet.MimeMessage(session);
            message.setFrom(new javax.mail.internet.InternetAddress("sistemaentradasunmsm@gmail.com"));
            message.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(correo));
            message.setSubject("Código de Verificación - Registro");
            message.setText("Tu código de verificación es: " + codigoGenerado);
            
            javax.mail.Transport.send(message);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Error al enviar el correo. Revisa la configuración de red y la contraseña de aplicación.");
            return;
        }
        
        boolean verificado = false;
        while (!verificado) {
            String input = javax.swing.JOptionPane.showInputDialog(vistaRegistro, "Se ha enviado un código de 4 dígitos a " + correo + ".\nIngresa el código de Verificación:");
            if (input == null) {
                return;
            }
            if (input.equals(codigoGenerado)) {
                verificado = true;
            } else {
                javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Código incorrecto. Vuelve a intentarlo.");
            }
        }

        boolean registrado = auth.registrarCliente(nombres, apellidos, dni, correo, contrasena);

        if (registrado) {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "Registro realizado con éxito.");
            vistaRegistro.txtNombres.setText("");
            vistaRegistro.txtApellidos.setText("");
            vistaRegistro.txtDni.setText("");
            vistaRegistro.jTextField1.setText("");
            vistaRegistro.txtContrasena.setText("");
            vistaRegistro.txtConfirmarContrasena.setText("");
            if (txtCorreo != null) txtCorreo.setText("");
            volverLogin();
        } else {
            javax.swing.JOptionPane.showMessageDialog(vistaRegistro, "No se pudo registrar. Puede que el DNI ya exista.");
        }
    }

    private void volverLogin() {
        vistaRegistro.dispose();
        vistaLogin.setSize(700, 520);
        vistaLogin.setResizable(false);
        vistaLogin.setLocationRelativeTo(null);
        vistaLogin.setVisible(true);
    }

    private void toggleContrasena() {
        if (vistaRegistro.jCheckBox1.isSelected()) {
            vistaRegistro.txtContrasena.setEchoChar((char) 0);
            vistaRegistro.txtConfirmarContrasena.setEchoChar((char) 0);
        } else {
            vistaRegistro.txtContrasena.setEchoChar('*');
            vistaRegistro.txtConfirmarContrasena.setEchoChar('*');
        }
    }

    private javax.swing.JTextField obtenerTxtCorreo() {
        try {
            java.awt.Component[] components = vistaRegistro.getContentPane().getComponents();
            javax.swing.JLabel targetLabel = null;
            for (java.awt.Component comp : components) {
                if (comp instanceof javax.swing.JLabel) {
                    javax.swing.JLabel lbl = (javax.swing.JLabel) comp;
                    if (lbl.getText() != null && lbl.getText().toLowerCase().contains("correo")) {
                        targetLabel = lbl;
                        break;
                    }
                }
            }
            if (targetLabel != null) {
                javax.swing.JTextField closestText = null;
                int minDistance = Integer.MAX_VALUE;
                for (java.awt.Component comp : components) {
                    if (comp instanceof javax.swing.JTextField && !(comp instanceof javax.swing.JPasswordField)) {
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