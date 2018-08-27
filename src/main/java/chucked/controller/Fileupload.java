package chucked.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import chucked.Repository.DemoRepository;
import chucked.model.MessageFormat;

@CrossOrigin
@RestController
public class Fileupload 
{
	
	@Autowired
	DemoRepository DBRepository;
	@Autowired
	KafkaTemplate<String,MessageFormat> kafkarepo;
	 public static final String UPLOAD_DIR = "upload_dir";
	 private static final String TOPIC = "javainuse-topic";

	@RequestMapping(value = "/upload", method = RequestMethod.POST )
	public @ResponseBody String fileupload(@RequestParam("name") String name,@RequestParam("file") MultipartFile file)
	{
		if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                System.out.println(bytes.length);
                String str = new String(bytes, StandardCharsets.UTF_8);
                	System.out.println(file.getName());
                	System.out.println(file.getContentType());
                	System.out.println(file.getSize());
                	System.out.println(file.getOriginalFilename() );
                
                
                return "You successfully uploaded " + name + " into " + name + "-uploaded !" + "\n" + str;
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
	}
	
	@RequestMapping(value = "/testupload", method = RequestMethod.POST )
	public void fileupload2( HttpServletRequest request, 
	        HttpServletResponse response) throws IOException, ServletException
	{
	       System.out.println("worked");
	}
	@RequestMapping(value = "/resupload", method = RequestMethod.POST )
	public void fileupload1( HttpServletRequest request, 
	        HttpServletResponse response) throws IOException, ServletException
	{
		int resumableChunkNumber        = getResumableChunkNumber(request);

        ResumableInfo info = getResumableInfo(request);
        RandomAccessFile raf = new RandomAccessFile(info.resumableFilePath, "rw");

        //Seek to position
        raf.seek((resumableChunkNumber - 1) * (long)info.resumableChunkSize);
       
        InputStream is = request.getInputStream();
        long readed = 0;
        long content_length = request.getContentLength();
        byte[] bytes = new byte[1024 * 100];
        while(readed < content_length) {
            int r = is.read(bytes);
            if (r < 0)  {
                break;
            }
            raf.write(bytes, 0, r);
            readed += r;
        }
        raf.close();
        
     
        
        info.uploadedChunks.add(new ResumableInfo.ResumableChunkNumber(resumableChunkNumber));
        if (info.checkIfUploadFinished()) { //Check if all chunks uploaded, and change filename
            ResumableInfoStorage.getInstance().remove(info);
            response.getWriter().print("All finished.");
        } else {
            response.getWriter().print("Upload");
        }
	}
	
	@RequestMapping(value = "/dbupload", method = RequestMethod.POST )
	public void fileuploaddb( HttpServletRequest request, 
	        HttpServletResponse response) throws IOException, ServletException
	{
		int resumableChunkNumber        = getResumableChunkNumber(request);

        ResumableInfo info = getResumableInfo(request);
        

       
        InputStream is = request.getInputStream();
        long readed = 0;
        long content_length = request.getContentLength();
        byte[] bytes = new byte[1024 * 100];
        while(readed < content_length) {
            int r = is.read(bytes);
            if (r < 0)  {
                break;
            }
  
            readed += r;
        }
        
        String str = new String(bytes, StandardCharsets.UTF_8);
         MessageFormat msg = new MessageFormat();
         msg.setChucknumber(resumableChunkNumber);
         msg.setTotalchuck((int) Math.ceil(((double) info.resumableTotalSize) / ((double) info.resumableChunkSize)));
         msg.setMessgaid(info.resumableFilename);
         msg.setMessage(str);
         DBRepository.save(msg);
        
        info.uploadedChunks.add(new ResumableInfo.ResumableChunkNumber(resumableChunkNumber));
        if (info.checkIfUploadFinished()) { //Check if all chunks uploaded, and change filename
            ResumableInfoStorage.getInstance().remove(info);
            response.getWriter().print("All finished.");
        } else {
            response.getWriter().print("Upload");
        }
	}
	
	@RequestMapping(value = "/kafka")
	public void Kafkaupload( HttpServletRequest request, 
	        HttpServletResponse response) throws IOException, ServletException
	{
		int resumableChunkNumber        = getResumableChunkNumber(request);

        ResumableInfo info = getResumableInfo(request);
        

       
        InputStream is = request.getInputStream();
        long readed = 0;
        long content_length = request.getContentLength();
        byte[] bytes = new byte[1024 * 100];
        while(readed < content_length) {
            int r = is.read(bytes);
            if (r < 0)  {
                break;
            }
  
            readed += r;
        }
        
        String str = new String(bytes, StandardCharsets.UTF_8);
         MessageFormat msg = new MessageFormat();
         msg.setChucknumber(resumableChunkNumber);
         msg.setTotalchuck((int) Math.ceil(((double) info.resumableTotalSize) / ((double) info.resumableChunkSize)));
         msg.setMessgaid(info.resumableFilename);
         msg.setMessage(str);
         kafkarepo.send(TOPIC,msg);
        
        info.uploadedChunks.add(new ResumableInfo.ResumableChunkNumber(resumableChunkNumber));
        if (info.checkIfUploadFinished()) { //Check if all chunks uploaded, and change filename
            ResumableInfoStorage.getInstance().remove(info);
            response.getWriter().print("All finished.");
        } else {
            response.getWriter().print("Upload");
        }
	}
	 private int getResumableChunkNumber(HttpServletRequest request) {
	        return HttpUtils.toInt(request.getParameter("resumableChunkNumber"), -1);
	    }

	 
	 
	 protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	        int resumableChunkNumber        = getResumableChunkNumber(request);

	        ResumableInfo info = getResumableInfo(request);

	        if (info.uploadedChunks.contains(new ResumableInfo.ResumableChunkNumber(resumableChunkNumber))) {
	            response.getWriter().print("Uploaded."); //This Chunk has been Uploaded.
	        } else {
	            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        }
	    }
	 
	 
	    private ResumableInfo getResumableInfo(HttpServletRequest request) throws ServletException {
	        String base_dir = UPLOAD_DIR;

	        int resumableChunkSize          = HttpUtils.toInt(request.getParameter("resumableChunkSize"), -1);
	        long resumableTotalSize         = HttpUtils.toLong(request.getParameter("resumableTotalSize"), -1);
	        String resumableIdentifier      = request.getParameter("resumableIdentifier");
	        String resumableFilename        = request.getParameter("resumableFilename");
	        String resumableRelativePath    = request.getParameter("resumableRelativePath");
	        //Here we add a ".temp" to every upload file to indicate NON-FINISHED
	        new File(base_dir).mkdir();
	        String resumableFilePath        = new File(base_dir, resumableFilename).getAbsolutePath() + ".temp";

	        ResumableInfoStorage storage = ResumableInfoStorage.getInstance();

	        ResumableInfo info = storage.get(resumableChunkSize, resumableTotalSize,
	                resumableIdentifier, resumableFilename, resumableRelativePath, resumableFilePath);
	        if (!info.vaild())         {
	            storage.remove(info);
	            throw new ServletException("Invalid request params.");
	        }
	        return info;
	    }
}
