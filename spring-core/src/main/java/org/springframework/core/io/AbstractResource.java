package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.springframework.core.NestedIOException;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

//抽象资源
public abstract class AbstractResource implements Resource {

    //是否存在
    @Override
    public boolean exists() {
        // Try file existence: can we find the file in the file system?
        try {
            return getFile().exists();
        } catch (IOException ex) {
            // Fall back to stream existence: can we open the stream?
            try {
                InputStream is = getInputStream();
                is.close();
                return true;
            } catch (Throwable isEx) {
                return false;
            }
        }
    }

    //是否可读
    @Override
    public boolean isReadable() {
        return true;
    }

    //是否打开
    @Override
    public boolean isOpen() {
        return false;
    }

    //获取URL
    @Override
    public URL getURL() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
    }

    //获取URI
    @Override
    public URI getURI() throws IOException {
        URL url = getURL();
        try {
            return ResourceUtils.toURI(url);
        } catch (URISyntaxException ex) {
            throw new NestedIOException("Invalid URI [" + url + "]", ex);
        }
    }

    //获取文件
    @Override
    public File getFile() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
    }

    /**
     * This implementation reads the entire InputStream to calculate the
     * content length. Subclasses will almost always be able to provide
     * a more optimal version of this, e.g. checking a File length.
     *
     * @see #getInputStream()
     */
    @Override
    public long contentLength() throws IOException {
        InputStream is = getInputStream();
        Assert.state(is != null, "Resource InputStream must not be null");
        try {
            long size = 0;
            byte[] buf = new byte[255];
            int read;
            while ((read = is.read(buf)) != -1) {
                size += read;
            }
            return size;
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
            }
        }
    }

    //最后修改时间戳
    @Override
    public long lastModified() throws IOException {
        long lastModified = getFileForLastModifiedCheck().lastModified();
        if (lastModified == 0L) {
            throw new FileNotFoundException(getDescription() +
                    " cannot be resolved in the file system for resolving its last-modified timestamp");
        }
        return lastModified;
    }

    /**
     * Determine the File to use for timestamp checking.
     * <p>The default implementation delegates to {@link #getFile()}.
     *
     * @return the File to use for timestamp checking (never {@code null})
     * @throws FileNotFoundException if the resource cannot be resolved as
     *                               an absolute file path, i.e. is not available in a file system
     * @throws IOException           in case of general resolution/reading failures
     */
    protected File getFileForLastModifiedCheck() throws IOException {
        return getFile();
    }

    /**
     * This implementation throws a FileNotFoundException, assuming
     * that relative resources cannot be created for this resource.
     */
    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
    }

    //获取文件名称
    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this ||
                (obj instanceof Resource && ((Resource) obj).getDescription().equals(getDescription())));
    }

    @Override
    public int hashCode() {
        return getDescription().hashCode();
    }

}
