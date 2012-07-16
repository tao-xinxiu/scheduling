/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.ow2.proactive_grid_cloud_portal.cli.ResponseStatus.OK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.type.TypeReference;
import org.ow2.proactive_grid_cloud_portal.cli.json.JobStateView;
import org.ow2.proactive_grid_cloud_portal.cli.json.SchedulerStateView;
import org.ow2.proactive_grid_cloud_portal.cli.utils.ArrayFormatter;

public class ListJobsCommand extends AbstractCommand implements Command {

	public ListJobsCommand() {
	}

	@Override
	public void execute() throws Exception {
		String resourceUrl = resourceUrl("revisionandstate");
		HttpGet request = new HttpGet(resourceUrl);
		HttpResponse response = execute(request);

		if (statusCode(OK) == statusCode(response)) {
			Map<Long, SchedulerStateView> stateMap = readValue(response,
					new TypeReference<Map<Long, SchedulerStateView>>() {
					});
			SchedulerStateView state = stateMap.entrySet().iterator().next()
					.getValue();

			ArrayFormatter oaf = new ArrayFormatter();
			oaf.setMaxColumnLength(30);
			oaf.setSpace(4);

			List<String> columnNames = new ArrayList<String>();
			columnNames.add("ID");
			columnNames.add("NAME");
			columnNames.add("OWNER");
			columnNames.add("PRIORITY");
			columnNames.add("PROJECT");
			columnNames.add("STATUS");
			columnNames.add("START AT");
			columnNames.add("DURATION");
			oaf.setTitle(columnNames);

			oaf.addEmptyLine();

			List<JobStateView> pendingJobs = Arrays.asList(state
					.getPendingJobs());
			Collections.sort(pendingJobs);
			List<JobStateView> runningJobs = Arrays.asList(state
					.getRunningJobs());
			Collections.sort(runningJobs);
			List<JobStateView> finishedJobs = Arrays.asList(state
					.getFinishedJobs());
			Collections.sort(finishedJobs);

			for (JobStateView js : finishedJobs) {
				oaf.addLine(rowList(js));
			}

			if (runningJobs.size() > 0) {
				oaf.addEmptyLine();
			}
			for (JobStateView js : state.getRunningJobs()) {
				oaf.addLine(rowList(js));
			}

			if (pendingJobs.size() > 0) {
				oaf.addEmptyLine();
			}
			for (JobStateView js : pendingJobs) {
				oaf.addLine(rowList(js));
			}

			writeLine(oaf.getAsString());

		} else {
			handleError(
					"An error occured while retriving jobs list from the scheduler ..",
					response);
		}
	}

	private List<String> rowList(JobStateView js) {
		List<String> row = new ArrayList<String>();
		row.add(String.valueOf(js.getId()));
		row.add(js.getName());
		row.add(js.getOwner());
		row.add(js.getPriority().toString());
		row.add(js.getProjectName());
		row.add(js.getJobInfo().getStatus());

		String date = formattedDate(js.getJobInfo().getStartTime());
		if (js.getJobInfo().getStartTime() != -1)
			date += " (" + formattedElapsedTime(js.getJobInfo().getStartTime())
					+ ")";
		row.add(date);
		row.add(formattedDuration(js.getJobInfo().getStartTime(), js
				.getJobInfo().getFinishedTime()));

		return row;
	}

}
