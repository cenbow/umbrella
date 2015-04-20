package com.harmony.umbrella.jaxws;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(serviceName = "HelloService")
public interface HelloService {

    String sayHi(@WebParam(name = "name") String name);

}