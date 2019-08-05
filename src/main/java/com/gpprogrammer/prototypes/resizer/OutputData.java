
package com.gpprogrammer.prototypes.resizer;

/**
 *
 * @author Tomasz Zajac 
 */
public class OutputData 
{
    public final int width;
    public final int height;
    public final int saveFormat;
    public OutputData(int width, int height, int saveFormat)
    {
        this.width = width;
        this.height = height;
        this.saveFormat = saveFormat;
    }
    
    @Override
    public String toString()
    {
        return "OutputData{width:"+width+", height:"+height+", saveFormat:"+saveFormat+"}";
    }
}
