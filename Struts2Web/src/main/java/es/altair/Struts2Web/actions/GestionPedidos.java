package es.altair.Struts2Web.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

import es.altair.Struts2Web.bean.Pedido;
import es.altair.Struts2Web.bean.Usuario;
import es.altair.Struts2Web.dao.PedidoDAO;
import es.altair.Struts2Web.dao.PedidoImpl;

public class GestionPedidos extends ActionSupport implements SessionAware{

	private SessionMap<String, Object> session;
	
	private ArrayList<Pedido> pedidos = new ArrayList<Pedido>();
	
	private Integer idPedido;
	
	private InputStream inputStream;
	
	private String contentDisposition;
	
	private String mensaje;
	
	private PedidoDAO peDAO = new PedidoImpl();
	
	public String listar() {
		pedidos = peDAO.listarPedidos(((Usuario)session.get("usulogeado")));
		
		return ActionSupport.SUCCESS;
	}
	
	public String mostrarPDF() {
		Pedido p = peDAO.obtenerPedidoPorId(idPedido);
		
		byte[] b = p.getFichero();
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentLength(b.length);
		if (b!=null) {
			inputStream = new ByteArrayInputStream(b);
			contentDisposition = "filename=" + p.getDocumento();
		}
		return ActionSupport.SUCCESS;
	}
	
	public String enviar() {
		Pedido p = peDAO.obtenerPedidoPorId(idPedido);
		boolean enviado = enviarMail(p);
		if (enviado)
			mensaje = "Email Enviado Correctamente";
		else
			mensaje = "Error Enviando Correo";
		
		pedidos = peDAO.listarPedidos(((Usuario)session.get("usulogeado")));
		
		return ActionSupport.SUCCESS;
	}
	
	private boolean enviarMail(final Pedido p) {
		 // Create all the needed properties
        Properties connectionProperties = new Properties();
        // SMTP host
        connectionProperties.put("mail.smtp.host", "smtp.gmail.com");
        // Is authentication enabled
        connectionProperties.put("mail.smtp.auth", "true");
        // Is StartTLS enabled
        connectionProperties.put("mail.smtp.starttls.enable", "true");
        // SSL Port
        connectionProperties.put("mail.smtp.socketFactory.port", "465");
        // SSL Socket Factory class
        connectionProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        // SMTP port, the same as SSL port :)
        connectionProperties.put("mail.smtp.port", "465");
         
        System.out.print("Creating the session...");
         
        // Create the session
        Session session;
		try {
			session = Session.getDefaultInstance(connectionProperties,
			        new javax.mail.Authenticator() {    // Define the authenticator
			            protected PasswordAuthentication getPasswordAuthentication() {
			                return new PasswordAuthentication(p.getUsuario().getEmailFrom(),p.getUsuario().getPassFrom());
			            }
			        });
		} catch (Exception e1) {
			return false;
		}
         
        System.out.println("done!");
         
        // Create and send the message
        try {
            // Create the message
            Message message = new MimeMessage(session);
            // Set sender
            message.setFrom(new InternetAddress(p.getUsuario().getEmailFrom()));
            // Set the recipients
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(p.getUsuario().getEmailTo()));
            // Set message subject
            message.setSubject("Pedido " + p.getFecha());
            // Set message text
            message.setText("Pedido Cliente: " + p.getCliente().getNombre());
            
            // Attach PDF
            DataSource dataSource = new ByteArrayDataSource(p.getFichero(), "application/pdf");
            Multipart mp1 = new MimeMultipart();
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.setDataHandler(new DataHandler(dataSource));
            attachment.setFileName(p.getDocumento());
            mp1.addBodyPart(attachment);
            message.setContent(mp1);
             
            System.out.print("Sending message...");
            // Send the message
            Transport.send(message);
             
            System.out.println("done!");
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}

	public void setSession(Map<String, Object> session) {
		this.session = (SessionMap<String, Object>) session;
	}
	
	public ArrayList<Pedido> getPedidos() {
		return pedidos;
	}
	
	public void setPedidos(ArrayList<Pedido> pedidos) {
		this.pedidos = pedidos;
	}
	
	public void setIdPedido(Integer idPedido) {
		this.idPedido = idPedido;
	}
	
	public Integer getIdPedido() {
		return idPedido;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public String getContentDisposition() {
		return contentDisposition;
	}
	
	public void setContentDisposition(String contentDisposition) {
		this.contentDisposition = contentDisposition;
	}
	
	public String getMensaje() {
		return mensaje;
	}
	
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
}