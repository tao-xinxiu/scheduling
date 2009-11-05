/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.jmx.mbean;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * MBean interface representing the attributes and the KPI values to monitor the ProActive Scheduler
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public interface SchedulerWrapperAnonymMBean {

    /**
     * Returns the state of the scheduler.
     *
     * @return The state of the scheduler.
     */
    public String getSchedulerStatus();

    /**
     * Returns the number of users connected to the scheduler.
     * 
     * @return the number of users connected to the scheduler.
     */
    public int getNumberOfConnectedUsers();

    /**
     * Returns the total number of jobs.
     * 
     * @return the total number of jobs.
     */
    public int getTotalNumberOfJobs();

    /**
     * Returns the number of pending jobs of the scheduler.
     *
     * @return The number of pending jobs of the scheduler.
     */
    public int getNumberOfPendingJobs();

    /**
     * Returns the number of running jobs of the scheduler.
     *
     * @return The number of running jobs of the scheduler.
     */
    public int getNumberOfRunningJobs();

    /**
     * Returns the number of finished jobs of the scheduler.
     *
     * @return The number of finished jobs of the scheduler.
     */
    public int getNumberOfFinishedJobs();

    /**
     * Returns the total number of Tasks.
     * 
     * @return the total number of Tasks.
     */
    public int getTotalNumberOfTasks();

    /**
     * Returns the number of pending Tasks of the scheduler.
     *
     * @return The number of pending Tasks of the scheduler.
     */
    public int getNumberOfPendingTasks();

    /**
     * Returns the number of running Tasks of the scheduler.
     *
     * @return The number of running Tasks of the scheduler.
     */
    public int getNumberOfRunningTasks();

    /**
     * Returns the number of finished Tasks of the scheduler.
     *
     * @return The number of finished Tasks of the scheduler.
     */
    public int getNumberOfFinishedTasks();
}
