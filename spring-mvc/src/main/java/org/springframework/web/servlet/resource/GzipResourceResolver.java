package org.springframework.web.servlet.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

//GZip资源处理器
public class GzipResourceResolver extends AbstractResourceResolver {

    @Override
    protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
                                               List<? extends Resource> locations, ResourceResolverChain chain) {
        Resource resource = chain.resolveResource(request, requestPath, locations);
        if (resource == null || (request != null && !isGzipAccepted(request))) {
            return resource;
        }
        try {
            Resource gzipped = new GzippedResource(resource);
            if (gzipped.exists()) {
                return gzipped;
            }
        } catch (IOException ex) {
            logger.trace("No gzipped resource for [" + resource.getFilename() + "]", ex);
        }
        return resource;
    }

    private boolean isGzipAccepted(HttpServletRequest request) {
        String value = request.getHeader("Accept-Encoding");
        return (value != null && value.toLowerCase().contains("gzip"));
    }

    @Override
    protected String resolveUrlPathInternal(String resourceUrlPath, List<? extends Resource> locations,
                                            ResourceResolverChain chain) {
        return chain.resolveUrlPath(resourceUrlPath, locations);
    }


    private static final class GzippedResource extends AbstractResource implements EncodedResource {

        private final Resource original;

        private final Resource gzipped;

        public GzippedResource(Resource original) throws IOException {
            this.original = original;
            this.gzipped = original.createRelative(original.getFilename() + ".gz");
        }

        public InputStream getInputStream() throws IOException {
            return this.gzipped.getInputStream();
        }

        public boolean exists() {
            return this.gzipped.exists();
        }

        public boolean isReadable() {
            return this.gzipped.isReadable();
        }

        public boolean isOpen() {
            return this.gzipped.isOpen();
        }

        public URL getURL() throws IOException {
            return this.gzipped.getURL();
        }

        public URI getURI() throws IOException {
            return this.gzipped.getURI();
        }

        public File getFile() throws IOException {
            return this.gzipped.getFile();
        }

        public long contentLength() throws IOException {
            return this.gzipped.contentLength();
        }

        public long lastModified() throws IOException {
            return this.gzipped.lastModified();
        }

        public Resource createRelative(String relativePath) throws IOException {
            return this.gzipped.createRelative(relativePath);
        }

        public String getFilename() {
            return this.original.getFilename();
        }

        public String getDescription() {
            return this.gzipped.getDescription();
        }

        public String getContentEncoding() {
            return "gzip";
        }
    }

}
