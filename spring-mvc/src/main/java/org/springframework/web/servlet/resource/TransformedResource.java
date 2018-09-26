package org.springframework.web.servlet.resource;

import java.io.IOException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

//转换的资源
public class TransformedResource extends ByteArrayResource {

	private final String filename;    //文件名
	private final long lastModified;  //最后修改时间

	//构造器
	public TransformedResource(Resource original, byte[] transformedContent) {
		super(transformedContent);
		this.filename = original.getFilename();
		try {
			this.lastModified = original.lastModified();
		}
		catch (IOException ex) {
			// should never happen
			throw new IllegalArgumentException(ex);
		}
	}

	//获取文件名
	@Override
	public String getFilename() {
		return this.filename;
	}

	//获取最后修改时间
	@Override
	public long lastModified() throws IOException {
		return this.lastModified;
	}

}