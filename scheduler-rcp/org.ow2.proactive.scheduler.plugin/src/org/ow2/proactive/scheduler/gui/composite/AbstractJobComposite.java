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
package org.ow2.proactive.scheduler.gui.composite;

import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;
import org.ow2.proactive.scheduler.gui.Activator;
import org.ow2.proactive.scheduler.gui.Colors;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.data.TableManager;
import org.ow2.proactive.scheduler.gui.views.JobInfo;
import org.ow2.proactive.scheduler.gui.views.ResultPreview;
import org.ow2.proactive.scheduler.gui.views.TaskView;


/**
 * This class represents a composite which will be able to display many
 * information of a list of jobs.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public abstract class AbstractJobComposite extends Composite {

    /* the unique id fort the "pending" table */
    public static final int PENDING_TABLE_ID = 0;

    /* the unique id fort the "running" table */
    public static final int RUNNING_TABLE_ID = 1;

    /* the unique id fort the "finished" table */
    public static final int FINISHED_TABLE_ID = 2;

    /* the unique id and the title for the column "Id" */
    public static final String COLUMN_ID_TITLE = "Id";

    /* the unique id and the title for the column "Priority" */
    public static final String COLUMN_PRIORITY_TITLE = "Priority";

    /* the unique id and the title for the column "Name" */
    public static final String COLUMN_NAME_TITLE = "Name";

    /* the unique id and the title for the column "User" */
    public static final String COLUMN_OWNER_TITLE = "User";

    /* the unique id and the title for the column "State" */
    public static final String COLUMN_STATE_TITLE = "State";

    /* the jobs failed background color */
    public static final Color JOB_FAILED_BACKGROUND_COLOR = Colors.RED;

    /* the jobs canceled background color */
    public static final Color JOB_CANCELED_BACKGROUND_COLOR = Colors.DARK_ORANGE;
    private Label label = null;
    private Table table = null;
    private int count = 0;
    private String title = null;
    private int order = JobState.ASC_ORDER;
    private int lastSorting = JobState.SORT_BY_ID;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /*
     * This is the default constructor
     *
     * @param parent the parent
     *
     * @param title a title
     *
     * @param jobsController an instance of jobsController
     *
     * @param tableId an unique id for the table
     */
    public AbstractJobComposite(Composite parent, String title, int tableId) {
        super(parent, SWT.NONE);
        this.setLayout(new GridLayout());
        this.title = title;
        this.label = createLabel(parent, title);
        this.table = createTable(parent, tableId);
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- private ------------------------------ //
    // -------------------------------------------------------------------- //
    private void setCount(int count) {
        if (!this.isDisposed()) {
            final int c = count;
            getDisplay().asyncExec(new Runnable() {
                public void run() {
                    label.setText(title + " (" + c + ")");
                }
            });
        }
    }

    private void refreshTable() {
        if (!isDisposed()) {
            // Turn off drawing to avoid flicker
            table.setRedraw(false);

            // We remove all the table entries and then add the new entries
            table.removeAll();

            Vector<JobId> jobsId = getJobs();
            int i = 0;
            for (JobId jobId : jobsId) {
                addJobInTable(jobId, i++);
            }

            // Turn drawing back on
            table.setRedraw(true);
        }
    }

    private void addJobInTable(JobId jobId, int anItemIndex) {
        if (!isDisposed()) {
            final JobState job = JobsController.getLocalView().getJobById(jobId);
            final int itemIndex = anItemIndex;
            getDisplay().syncExec(new Runnable() {
                public void run() {
                    createItem(job, itemIndex);
                }
            });
        }
    }

    private void sort(SelectionEvent event, int field) {
        if (lastSorting == field) {
            order = (order == JobState.DESC_ORDER) ? JobState.ASC_ORDER : JobState.DESC_ORDER;
            JobState.setSortingOrder(order);
        }
        JobState.setSortingBy(field);
        lastSorting = field;

        sortJobs();

        refreshTable();
        table.setSortColumn((TableColumn) event.widget);
        table.setSortDirection((order == JobState.DESC_ORDER) ? SWT.DOWN : SWT.UP);
    }

    private void fillBackgroundColor(TableItem item, JobStatus status, Color col) {
        boolean setFont = false;
        switch (status) {
            case CANCELED:
                setFont = true;
                item.setForeground(JOB_CANCELED_BACKGROUND_COLOR);
                break;
            case FAILED:
                setFont = true;
                item.setForeground(JOB_FAILED_BACKGROUND_COLOR);
                break;
            case FINISHED:
            case PAUSED:
            case PENDING:
            case RUNNING:
            case STALLED:
        }
        if (setFont) {
            Font font = item.getFont();
            item.setFont(new Font(font.getDevice(), font.getFontData()[0].getName(), font.getFontData()[0]
                    .getHeight(), font.getFontData()[0].getStyle() | SWT.BOLD));
        }
    }

    // -------------------------------------------------------------------- //
    // ---------------------------- protected ----------------------------- //
    // -------------------------------------------------------------------- //
    /*
     * Create and return a Label
     *
     * @param parent the parent
     *
     * @param title a title
     *
     * @return
     */
    protected Label createLabel(Composite parent, String title) {
        Label label = new Label(this, SWT.CENTER);
        label.setText(title + " (" + count + ")");
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setForeground(Colors.RED);
        return label;
    }

    /*
     * Create and return a table
     *
     * @param parent the parent
     *
     * @param tableId an unique id for the table
     *
     * @return
     */
    protected Table createTable(Composite parent, int tableId) {
        table = new Table(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setData(tableId);

        // creating TableColumn
        TableColumn tc1 = new TableColumn(table, SWT.RIGHT);
        TableColumn tc4 = new TableColumn(table, SWT.LEFT);
        TableColumn tc5 = new TableColumn(table, SWT.LEFT);
        TableColumn tc2 = new TableColumn(table, SWT.CENTER);
        TableColumn tc3 = new TableColumn(table, SWT.CENTER);
        // addSelectionListener
        tc1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, JobState.SORT_BY_ID);
            }
        });
        tc2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, JobState.SORT_BY_PRIORITY);
            }
        });
        tc3.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, JobState.SORT_BY_NAME);
            }
        });
        tc4.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, JobState.SORT_BY_STATUS);
            }
        });
        tc5.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                sort(event, JobState.SORT_BY_OWNER);
            }
        });
        // setText
        tc1.setText(COLUMN_ID_TITLE);
        tc2.setText(COLUMN_PRIORITY_TITLE);
        tc3.setText(COLUMN_NAME_TITLE);
        tc4.setText(COLUMN_STATE_TITLE);
        tc5.setText(COLUMN_OWNER_TITLE);
        // setWidth
        tc1.setWidth(40);
        tc2.setWidth(70);
        tc3.setWidth(100);
        tc4.setWidth(90);
        tc5.setWidth(45);
        // setMoveable
        tc1.setMoveable(true);
        tc2.setMoveable(true);
        tc3.setMoveable(true);
        tc4.setMoveable(true);
        tc5.setMoveable(true);

        // MUST BE DONE BEFORE THE SECOND addListener !
        // register to the table manager
        TableManager.getInstance().add(table);

        table.addListener(SWT.Selection, new Listener() {
            private JobId lastSelected;

            public void handleEvent(Event event) {
                Widget widget = event.item;

                if ((widget != null) && (!widget.isDisposed())) {

                    // get the jobId
                    JobId jobId = (JobId) widget.getData();

                    // get the job by jobId
                    JobState job = JobsController.getLocalView().getJobById(jobId);

                    // FIXME should be controlled by boolean button
                    // show its output
                    // JobsOutputController.getInstance().showJobOutput(jobId);

                    List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();

                    // update its informations
                    JobInfo jobInfo = JobInfo.getInstance();
                    if (jobInfo != null) {
                        if (jobsId.isEmpty()) {
                            jobInfo.clear();
                        } else {
                            if (jobsId.size() == 1) {
                                jobInfo.updateInfos(job);
                            } else {
                                jobInfo.updateInfos(JobsController.getLocalView().getJobsByIds(jobsId));
                            }
                            if (lastSelected == null || !jobId.equals(lastSelected)) {
                                lastSelected = jobId;
                                TaskComposite.deleteTaskResultCache();
                            }
                        }

                        // set Focus on job info
                        IWorkbench iworkbench = PlatformUI.getWorkbench();
                        IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
                        IWorkbenchPage page = currentWindow.getActivePage();
                        try {
                            IViewPart part = page.showView(JobInfo.ID);
                            part.setFocus();
                        } catch (Exception e) {
                            Activator.log(IStatus.ERROR, "Error when showing the view.", e);
                            e.printStackTrace();
                        }
                    }

                    // update its tasks informations
                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        if (jobsId.isEmpty())
                            taskView.clear();
                        else if (jobsId.size() == 1)
                            taskView.fullUpdate(job);
                        else
                            taskView.fullUpdate(JobsController.getLocalView().getJobsByIds(jobsId));
                    }

                    ResultPreview resultPreview = ResultPreview.getInstance();
                    if (resultPreview != null) {
                        resultPreview.update(new SimpleTextPanel("No task selected"));
                    }

                    ActionsManager.getInstance().update();
                }
            }
        });
        return table;
    }

    /**
     * Create and return an item which will be added in the table. The item will has its data set to
     * the jobId of the job.
     *
     * @param job the job which represent the item
     *
     * @return the new item
     */
    protected TableItem createItem(JobState job, int itemIndex) {
        TableColumn[] cols = table.getColumns();
        TableItem item = new TableItem(table, SWT.NONE);
        item.setData(job.getId());
        if (itemIndex == 0) {
            fillBackgroundColor(item, job.getStatus(), null);
        } else {
            fillBackgroundColor(item, job.getStatus(), table.getItem(itemIndex - 1).getBackground());
        }

        for (int i = 0; i < cols.length; i++) {
            String title = cols[i].getText();
            if (title.equals(COLUMN_STATE_TITLE)) {
                if (job.getStatus().equals(JobStatus.RUNNING)) {
                    item.setText(i, job.getStatus() + " (" + job.getNumberOfRunningTasks() + ")");
                } else {
                    item.setText(i, job.getStatus().toString());
                }
            } else if (title.equals(COLUMN_ID_TITLE)) {
                item.setText(i, job.getId().toString());
            } else if (title.equals(COLUMN_PRIORITY_TITLE)) {
                item.setText(i, job.getPriority().toString());
            } else if (title.equals(COLUMN_NAME_TITLE)) {
                item.setText(i, job.getName());
            } else if (title.equals(COLUMN_OWNER_TITLE)) {
                item.setText(i, job.getOwner());
            }
        }
        return item;
    }

    protected void stateUpdate(JobId aJobId) {
        if (!this.isDisposed()) {
            final JobId jobId = aJobId;

            getDisplay().syncExec(new Runnable() {
                public void run() {
                    Table table = getTable();
                    TableItem[] items = table.getItems();
                    TableItem item = null;
                    int itemIndex = 0;
                    for (TableItem it : items) {
                        if (((JobId) (it.getData())).equals(jobId)) {
                            item = it;
                            break;
                        }
                        itemIndex++;
                    }

                    TableColumn[] cols = table.getColumns();
                    JobState job = JobsController.getLocalView().getJobById(jobId);
                    for (int i = 0; i < cols.length; i++) {
                        String title = cols[i].getText();
                        if ((title != null) && (title.equals(COLUMN_STATE_TITLE))) {
                            if (itemIndex == 0) {
                                fillBackgroundColor(item, job.getStatus(), null);
                            } else {
                                fillBackgroundColor(item, job.getStatus(), items[itemIndex].getBackground());
                            }
                            if (JobStatus.RUNNING.equals(job.getStatus())) {
                                item.setText(i, job.getStatus() + " (" + job.getNumberOfRunningTasks() + ")");
                            } else {
                                item.setText(i, job.getStatus().toString());
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    protected void priorityUpdate(JobId aJobId) {
        if (!this.isDisposed()) {
            final JobId jobId = aJobId;

            getDisplay().syncExec(new Runnable() {
                public void run() {
                    Table table = getTable();
                    TableItem[] items = table.getItems();
                    TableItem item = null;
                    for (TableItem it : items)
                        if (((JobId) (it.getData())).equals(jobId)) {
                            item = it;
                            break;
                        }

                    TableColumn[] cols = table.getColumns();
                    JobState job = JobsController.getLocalView().getJobById(jobId);
                    for (int i = 0; i < cols.length; i++) {
                        String title = cols[i].getText();
                        if ((title != null) && (title.equals(COLUMN_PRIORITY_TITLE))) {
                            item.setText(i, job.getPriority().toString());
                            break;
                        }
                    }
                }
            });
        }
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /*
     * To increase the count include in the label
     *
     * @return the current value used in the label
     */
    public int increaseCount() {
        setCount(++count);
        return count;
    }

    /*
     * To decrease the count include in the label
     *
     * @return the current value used in the label
     */
    public int decreaseCount() {
        if (count == 0) {
            return 0;
        }
        setCount(--count);
        return count;
    }

    /*
     * To get the table
     *
     * @return the table
     */
    public Table getTable() {
        return table;
    }

    /*
     * To add a job in the table by its jobId
     *
     * @param jobId the jobid of the job which will be added in the table
     */
    public void addJob(JobId jobId) {
        increaseCount();
        addJobInTable(jobId, count - 1);
    }

    /*
     * To remove a job in the table by its jobId
     *
     * @param jobId the jobid of the job which will be removed in the table
     */
    public void removeJob(JobId jobId) {
        if (!isDisposed()) {
            Vector<JobId> jobsId = getJobs();
            int tmp = -1;
            for (int i = 0; i < jobsId.size(); i++) {
                if (jobsId.get(i).equals(jobId)) {
                    tmp = i;
                    break;
                }
            }
            final int i = tmp;
            getDisplay().syncExec(new Runnable() {
                public void run() {
                    int[] j = table.getSelectionIndices();

                    table.remove(i);

                    if (j.length == 1) {
                        if (i == j[0]) {
                            JobInfo jobInfo = JobInfo.getInstance();
                            if (jobInfo != null) {
                                jobInfo.clear();
                            }

                            ResultPreview resultPreview = ResultPreview.getInstance();
                            if (resultPreview != null) {
                                resultPreview.update(new SimpleTextPanel("No selected task"));
                            }

                            TaskView taskView = TaskView.getInstance();
                            if (taskView != null) {
                                taskView.clear();
                            }
                        }
                    } else if (j.length > 1) {
                        List<JobState> jobs = JobsController.getLocalView().getJobsByIds(
                                TableManager.getInstance().getJobsIdOfSelectedItems());

                        JobInfo jobInfo = JobInfo.getInstance();
                        if (jobInfo != null) {
                            jobInfo.updateInfos(jobs);
                        }

                        ResultPreview resultPreview = ResultPreview.getInstance();
                        if (resultPreview != null) {
                            resultPreview.update(new SimpleTextPanel("No selected task"));
                        }

                        TaskView taskView = TaskView.getInstance();
                        if (taskView != null) {
                            taskView.fullUpdate(jobs);
                        }
                    }
                    decreaseCount();

                    // enabling/disabling button permitted with this job
                    ActionsManager.getInstance().update();
                }
            });
        }
    }

    /*
     * To initialize the table at the beginning. This method set the count (include in the label) to
     * the jobs list size and refresh the table.
     */
    public void initTable() {
        count = getJobs().size();
        setCount(count);
        refreshTable();
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- abstract ----------------------------- //
    // -------------------------------------------------------------------- //
    /*
     * To obtain the jobs list
     *
     * @return jobs list
     */
    public abstract Vector<JobId> getJobs();

    /*
     * To sort jobs
     */
    public abstract void sortJobs();

    /*
     * To clear properly the composite. This method will be called on disconnection only.
     */
    public abstract void clear();

    // -------------------------------------------------------------------- //
    // ------------------------ extends composite ------------------------- //
    // -------------------------------------------------------------------- //
    /*
     * @see org.eclipse.swt.widgets.Control#setMenu(org.eclipse.swt.widgets.Menu)
     */
    @Override
    public void setMenu(Menu menu) {
        super.setMenu(menu);
        table.setMenu(menu);
        label.setMenu(menu);
    }

    /*
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (label != null) {
            label.setVisible(visible);
        }
        if (table != null) {
            table.setVisible(visible);
        }
    }
}
