package hudson.plugins.scm_sync_configuration.strategies.impl;

import hudson.XmlFile;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Saveable;
import hudson.model.User;
import hudson.plugins.scm_sync_configuration.model.MessageWeight;
import hudson.plugins.scm_sync_configuration.model.WeightedMessage;
import hudson.plugins.scm_sync_configuration.strategies.AbstractScmSyncStrategy;
import hudson.plugins.scm_sync_configuration.strategies.model.ClassAndFileConfigurationEntityMatcher;
import hudson.plugins.scm_sync_configuration.strategies.model.ConfigurationEntityMatcher;
import hudson.plugins.scm_sync_configuration.strategies.model.PageMatcher;

import java.util.ArrayList;
import java.util.List;

public class UserConfigScmSyncStrategy extends AbstractScmSyncStrategy {

    // Don't miss to take into account view urls since we can configure a job through a view !
	private static final List<PageMatcher> PAGE_MATCHERS = new ArrayList<PageMatcher>(){ {
        add(new PageMatcher("^securityRealm/addUser$", "#main-panel form"));
        add(new PageMatcher("^securityRealm/user/[^/]+/configure$", "form[name='config']"));
    } };
    // Only saving config.xml file located in user directory
	private static final String [] PATTERNS = new String[] {
        "users/*/config.xml"
	};
	private static final ConfigurationEntityMatcher CONFIG_ENTITY_MANAGER = new ClassAndFileConfigurationEntityMatcher(User.class, PATTERNS);

	public UserConfigScmSyncStrategy(){
		super(CONFIG_ENTITY_MANAGER, PAGE_MATCHERS);
	}

    public CommitMessageFactory getCommitMessageFactory(){
        return new CommitMessageFactory(){
            public WeightedMessage getMessageWhenSaveableUpdated(Saveable s, XmlFile file) {
                return new WeightedMessage("User ["+((User)s).getDisplayName()+"] configuration updated",
                        // Job config update message should be considered as "important", especially
                        // more important than the plugin descriptors Saveable updates
                        MessageWeight.IMPORTANT);
            }
            public WeightedMessage getMessageWhenItemRenamed(Item item, String oldPath, String newPath) {
                return new WeightedMessage("User ["+item.getName()+"] configuration renamed from ["+oldPath+"] to ["+newPath+"]",
                        // Job config rename message should be considered as "important", especially
                        // more important than the plugin descriptors Saveable renames
                        MessageWeight.MORE_IMPORTANT);
            }
            public WeightedMessage getMessageWhenItemDeleted(Item item) {
                return new WeightedMessage("User ["+item.getName()+"] hierarchy deleted",
                        // Job config deletion message should be considered as "important", especially
                        // more important than the plugin descriptors Saveable deletions
                        MessageWeight.MORE_IMPORTANT);
            }
        };
    }
}
