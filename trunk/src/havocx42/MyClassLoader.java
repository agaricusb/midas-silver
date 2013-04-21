/**
 * 
 */
package havocx42;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * @author Laurence
 *
 */
public class MyClassLoader extends URLClassLoader {

    /**
     * @param urls
     */
    public MyClassLoader(URL[] urls) {
        super(urls);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param urls
     * @param parent
     */
    public MyClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param urls
     * @param parent
     * @param factory
     */
    public MyClassLoader(URL[] urls, ClassLoader parent,
            URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        // TODO Auto-generated constructor stub
    }
    
    public void addURL(URL url) {  
        super.addURL(url);  
    } 

}
