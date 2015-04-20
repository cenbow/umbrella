/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.harmony.umbrella.monitor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.harmony.umbrella.monitor.MethodMonitor.MethodGraph;
import com.harmony.umbrella.monitor.annotation.Monitored;
import com.harmony.umbrella.monitor.util.MonitorUtils;

/**
 * @author wuxii@foxmail.com
 */
public class DefaultMethodGraph extends AbstractGraph implements MethodGraph {

	protected Object target;
	protected Method method;
	protected Object[] args;

	public DefaultMethodGraph() {
	}

	@Override
	public String getIdentifie() {
		if (identifie == null) {
			identifie = MonitorUtils.methodIdentifie(method);
		}
		return super.getIdentifie();
	}

	public DefaultMethodGraph(Object target, Method method, Object[] args) {
		this.target = target;
		this.method = method;
		this.args = args;
	}

	public Object getTarget() {
		return target;
	}

	@Override
	public Map<String, Object> getArguments() {
		Map<String, Object> arguments = new HashMap<String, Object>();
		if (args != null) {
			for (int i = 0, max = args.length; i < max; i++) {
				arguments.put(i + 1 + "", args[i]);
			}
		}
		return arguments;
	}

	@Override
	@Deprecated
	public void setArguments(Map<String, Object> arguments) {
		super.setArguments(arguments);
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public Object[] getArgs() {
		return args;
	}

	@Override
	public String getModule() {
		Monitored ann = method.getAnnotation(Monitored.class);
		if (ann != null) {
			return ann.module();
		}
		return null;
	}

	@Override
	public String getOperator() {
		Monitored ann = method.getAnnotation(Monitored.class);
		if (ann != null) {
			return ann.operator();
		}
		return null;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(args);
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultMethodGraph other = (DefaultMethodGraph) obj;
        if (!Arrays.equals(args, other.args))
            return false;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }

}
