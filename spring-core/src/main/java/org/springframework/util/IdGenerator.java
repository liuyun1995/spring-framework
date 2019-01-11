package org.springframework.util;

import java.util.UUID;

//ID生成器
public interface IdGenerator {

	//获取ID
	UUID generateId();

}
