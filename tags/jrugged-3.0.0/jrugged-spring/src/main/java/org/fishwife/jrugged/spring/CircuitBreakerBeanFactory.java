/* Copyright 2009-2011 Comcast Interactive Media, LLC.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.fishwife.jrugged.spring;

import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.CircuitBreakerConfig;
import org.fishwife.jrugged.CircuitBreakerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExporter;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Factory to create new {@link CircuitBreakerBean} instances and keep track of
 * them. If a {@link MBeanExporter} is set, then the CircuitBreakerBean will be
 * automatically exported as a JMX MBean.
 */
public class CircuitBreakerBeanFactory extends CircuitBreakerFactory {

    @Autowired(required=false)
    private MBeanExporter mBeanExporter;

    /**
     * Set the {@link MBeanExporter} to use to export {@link CircuitBreakerBean}
     * instances as JMX MBeans.
     * @param mBeanExporter the {@link MBeanExporter} to set.
     */
    public void setMBeanExporter(MBeanExporter mBeanExporter) {
        this.mBeanExporter = mBeanExporter;
    }

    /**
     * Create a new {@link CircuitBreakerBean} and map it to the provided value.
     * If the {@link MBeanExporter} is set, then the CircuitBreakerBean will be
     * exported as a JMX MBean.
     * If the CircuitBreaker already exists, then the existing instance is
     * returned.
     * @param name the value for the {@link org.fishwife.jrugged.CircuitBreaker}
     * @param config the {@link org.fishwife.jrugged.CircuitBreakerConfig}
     */
    public synchronized CircuitBreaker createCircuitBreaker(String name,
            CircuitBreakerConfig config) {

        CircuitBreaker circuitBreaker = findCircuitBreaker(name);

        if (circuitBreaker == null) {
            circuitBreaker = new CircuitBreakerBean(name);

            configureCircuitBreaker(name, circuitBreaker, config);

            if (mBeanExporter != null) {
                ObjectName objectName;

                try {
                    objectName = new ObjectName(
                            "org.fishwife.jrugged.spring:type=CircuitBreakerBean," +
                                    "value=" + name);
                }
                catch (MalformedObjectNameException e) {
                    throw new IllegalArgumentException("Invalid MBean Name " +
                            name, e);

                }

                mBeanExporter.registerManagedResource(circuitBreaker, objectName);
            }

            addCircuitBreakerToMap(name, circuitBreaker);
        }

        return circuitBreaker;
    }

    /**
     * Find an existing {@link CircuitBreakerBean}
     * @param name the value for the {@link CircuitBreakerBean}
     * @return the found {@link CircuitBreakerBean}, or null if it is not found.
     */
    public CircuitBreakerBean findCircuitBreakerBean(String name) {
        CircuitBreaker performanceMonitor = findCircuitBreaker(name);

        if (performanceMonitor instanceof CircuitBreakerBean) {
            return (CircuitBreakerBean)performanceMonitor;
        }
        return null;
    }
}