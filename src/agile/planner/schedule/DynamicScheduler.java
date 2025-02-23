package agile.planner.schedule;

import agile.planner.schedule.day.Day;
import agile.planner.data.Task;
import agile.planner.user.UserConfig;
import agile.planner.util.EventLog;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Provides a more dynamic solution with generating a schedule
 *
 * @author Andrew Roe
 * @author Lucia Langaney
 */
public class DynamicScheduler implements Scheduler {

    /** Holds relevant data for user settings in scheduling */
    private final UserConfig userConfig;
    /** EventLog for logging data on Day actions */
    private EventLog eventLog;

    /**
     * Primary constructor for CompactScheduler
     *
     * @param userConfig user settings for scheduling purposes
     * @param eventLog EventLog for logging data on Day actions
     */
    public DynamicScheduler(UserConfig userConfig, EventLog eventLog) {
        this.userConfig = userConfig;
        this.eventLog = eventLog;

    }

    @Override
    public int assignDay(Day day, int errorCount, PriorityQueue<Task> complete, PriorityQueue<Task> taskManager) {
        PriorityQueue<Task> incomplete = new PriorityQueue<>();
        int numErrors = errorCount;
        while(day.hasSpareHours() && taskManager.size() > 0) {
            Task task = taskManager.remove();
            boolean validTaskStatus = day.addSubTask(task);
            if(task.getSubTotalHoursRemaining() > 0) {
                eventLog.reportDayAction(day, task, validTaskStatus);
                incomplete.add(task);
            } else {
                complete.add(task);
                eventLog.reportDayAction(day, task, validTaskStatus);
                numErrors += validTaskStatus ? 0 : 1;
                if(!day.hasSpareHours()) {
                    while(taskManager.size() > 0 && taskManager.peek().getDueDate().equals(day.getDate())) {
                        Task dueTask = taskManager.remove();
                        complete.add(dueTask);
                        day.addSubTask(dueTask);
                        eventLog.reportDayAction(day, dueTask, validTaskStatus);
                        numErrors++;
                    }
                }
            }
        }
        while(incomplete.size() > 0) {
            taskManager.add(incomplete.remove());
        }
        return numErrors;
    }

    @Override
    public boolean correctSchedule(List<Day> schedule, int errorCount) {
        return false;
    }
}
