package org.everit.jira.timetracker.plugin;

import com.atlassian.jira.issue.MutableIssue;

/**
 * The Jira Timetracker plugin utils class.
 */
public final class JiraTimetrackerUtil {

    /**
     * Check the issue original estimated time. If null then the original estimated time wasn't specified, else compare
     * the spent time whit the original estimated time.
     * 
     * @param issue
     *            The issue.
     * @return True if not specified, bigger or equals whit spent time else false.
     */
    public static boolean checkIssueEstimatedTime(final MutableIssue issue) {
        Long originalEstimated = issue.getOriginalEstimate();
        Long timeSpent = issue.getTimeSpent();
        if (originalEstimated != null) {
            if (timeSpent > originalEstimated) {
                return false;
            }
        }
        return true;
    }

}
