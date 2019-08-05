
package com.gpprogrammer.prototypes.resizer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 *
 * @author Tomasz Zajac
 */
public class ImageResizer implements ControlsListener{
  
    private static final Pattern FILE_NAME_SLICER = Pattern.compile("(?<fname>.*)(?:\\.)(?<ext>.*$)");
    private static final Pattern VALIDATE_FILE_EXT = Pattern.compile("(?:^.*)(\\.(jpg|png))");
    private static final String[] OUTPUT_FILE_EXTS =  {null, "jpg", "png"};
    
     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
         new ImageResizer();
    }
   
    private final FileDrop fd;
    private final ControlsPanel cp;
    private final javax.swing.JFrame frame;
    private final Map<File, String> foundFiles;
    private  OutputData opd;
    
    public ImageResizer()
    {    
       foundFiles = new HashMap<>();
       cp = new ControlsPanel();
       log("Log:");
       cp.addActionListener(this);
       frame = new javax.swing.JFrame( "Image Resizer" );
       frame.setMinimumSize( new Dimension(850, 600));
       frame.getContentPane().add(cp, java.awt.BorderLayout.CENTER ); 
       fd = new FileDrop( System.out, cp.dropTarget(), new FileDrop.Listener() {
            @Override
            public void filesDropped(java.io.File[] files) {
                for (File file : files) {
                    try {
                        if (!foundFiles.containsKey(file)) {
                            String path = file.getCanonicalPath();
                            String fName = file.getName();
                            Matcher m = VALIDATE_FILE_EXT.matcher(fName);
                            if (m.find()) {
                                cp.dropTarget().append( fName+"\n" );
                                foundFiles.put(file, fName);
                            }
                        }
                    } // end try 
                    catch( java.io.IOException e ) {}
                } // end for: through each dropped file
                
                if(foundFiles.size() > 0)
                {
                    log(foundFiles.size()+" files dropped");
                    cp.setAssetsReady(true);
                } else 
                {
                     log("0 file dropped!");
                }
            } // end filesDropped
        }); // end FileDrop.Listener

        frame.setBounds( 0, 0, 800, 600 );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible(true);
    }
    
    private void log(String msg)
    {
        if(cp!=null)
        {
           cp.GetOutputPanel().append(msg+"\n");
        }   
    }
   
    @Override
    public void processFileList() 
    {
       log("processFileList");
       opd  = cp.getOutputData();
       log(opd.toString());
       foundFiles.forEach( (key, value) -> { processImnage(key);});
    }
    
    @Override
    public void reset() 
    {
       log("reset");
       foundFiles.clear();
       cp.reset();
    }
    
  public void processImnage(File fl) {

      if(opd == null)
      {
          //System.out.println("Output data is missing");
           log("Output data is missing");
          return;
      }
    try {

        BufferedImage originalImage = ImageIO.read(fl);
        
        System.out.println(originalImage.getWidth());
        System.out.println(originalImage.getHeight());
        log("processing: "+fl.getCanonicalPath());
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        int sValue = cp.getSizeValue();
        int _width = originalImage.getWidth();
        int _height = originalImage.getHeight();
        float _scale;
     
        float _scaleX = (float) opd.width / (float)_width;
        float _scaleY = (float) opd.height / (float)_height;
        if(_scaleX > _scaleY)
        {
            _width = opd.width;
            _height *= _scaleX;   
             System.out.println("A");
        }else
        {
           _width *= _scaleY; 
           _height = opd.height;    
           System.out.println("B");
        }

        BufferedImage resizeImageJpg = resizeImage(originalImage, type, _width,_height, (opd.width>0?opd.width:_width),opd.height>0?opd.height:_height);
        File outputDitr = new File(fl.getParent()+"\\_resized");
        if (!outputDitr.exists()) 
        {
            log(outputDitr.getCanonicalPath()+ " not found, creating ");
            try
            {
                outputDitr.mkdir(); 
            } 
            catch(SecurityException se)
            {    
                log("creating failed: "+se.getMessage());
            }        
        }
        
        String fname = fl.getName().toLowerCase(); 
        String ext = null;
        
        Matcher m1 = FILE_NAME_SLICER.matcher(fl.getName());
        if( m1.find())
        {
           fname = m1.group("fname");
           ext = ( OUTPUT_FILE_EXTS[opd.saveFormat]!=null ? OUTPUT_FILE_EXTS[opd.saveFormat]:m1.group("ext"));
        }
        
        if(outputDitr.exists()) 
        {    
            File outputFile = new File(outputDitr.getPath() + "//" + (ext!=null?fname+"."+ext:fname));
            ImageIO.write(resizeImageJpg, ext, outputFile); 
             
             if(outputFile.exists())
             {
                 log(" Success! Files saved! ");
             }else
             {
                 log("file not found after save");
             }
            
        }else
        {
            log("outputDitr not found :/");
        }
    } catch (IOException e) 
    {
       // System.out.println(e.getMessage());
        log("Writing image failed: "+e.getMessage());
    }
}

private BufferedImage resizeImage(BufferedImage originalImage, int type, int srcWidth, int srcHeight, int outputWidth, int outputHeight) {
    
        BufferedImage resizedImage = new BufferedImage(outputWidth, outputHeight, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, (outputWidth-srcWidth)/2, (outputHeight-srcHeight)/2, srcWidth , srcHeight, null);
        g.dispose();

        return resizedImage;
    }
}
