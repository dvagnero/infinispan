package org.infinispan.configuration.cache;

import static org.infinispan.configuration.cache.SitesConfiguration.DISABLE_BACKUPS;
import static org.infinispan.configuration.cache.SitesConfiguration.IN_USE_BACKUP_SITES;
import static org.infinispan.configuration.cache.SitesConfiguration.MERGE_POLICY;
import static org.infinispan.util.logging.Log.CONFIG;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.xsite.spi.XSiteEntryMergePolicy;
import org.infinispan.xsite.spi.XSiteMergePolicy;

/**
 * @author Mircea.Markus@jboss.com
 * @since 5.2
 */
public class SitesConfigurationBuilder extends AbstractConfigurationChildBuilder implements Builder<SitesConfiguration> {
   private final AttributeSet attributes;
   private final List<BackupConfigurationBuilder> backups = new ArrayList<>(2);
   private final BackupForBuilder backupForBuilder;


   public SitesConfigurationBuilder(ConfigurationBuilder builder) {
      super(builder);
      attributes = SitesConfiguration.attributeDefinitionSet();
      backupForBuilder = new BackupForBuilder(builder);
   }

   public BackupConfigurationBuilder addBackup() {
      BackupConfigurationBuilder bcb = new BackupConfigurationBuilder(getBuilder());
      backups.add(bcb);
      return bcb;
   }

   public List<BackupConfigurationBuilder> backups() {
      return backups;
   }

   /**
    * Returns true if this cache won't backup its data remotely.
    * It would still accept other sites backing up data on this site.
    */
   public SitesConfigurationBuilder disableBackups(boolean disable) {
      attributes.attribute(DISABLE_BACKUPS).set(disable);
      return this;
   }

   /**
    * Defines the site names, from the list of sites names defined within 'backups' element, to
    * which this cache backups its data.
    */
   public SitesConfigurationBuilder addInUseBackupSite(String site) {
      Set<String> sites = attributes.attribute(IN_USE_BACKUP_SITES).get();
      sites.add(site);
      attributes.attribute(IN_USE_BACKUP_SITES).set(sites);
      return this;
   }

   public BackupForBuilder backupFor() {
      return backupForBuilder;
   }

   /**
    * Configures the {@link XSiteEntryMergePolicy} to be used.
    * <p>
    * {@link XSiteEntryMergePolicy} is invoked when a conflicts is detected between 2 sites. A conflict happens when a
    * key is updated concurrently in 2 sites.
    * <p>
    * {@link XSiteMergePolicy} enum contains implementation available to be used.
    *
    * @param mergePolicy The {@link XSiteEntryMergePolicy} implementation to use.
    * @return {@code this}.
    * @see XSiteEntryMergePolicy
    * @see XSiteMergePolicy
    */
   public SitesConfigurationBuilder mergePolicy(XSiteEntryMergePolicy<?, ?> mergePolicy) {
      attributes.attribute(MERGE_POLICY).set(mergePolicy);
      return this;
   }

   @Override
   public void validate() {
      backupForBuilder.validate();

      //don't allow two backups with the same name
      Set<String> backupNames = new HashSet<>(backups.size());

      for (BackupConfigurationBuilder bcb : backups) {
         if (!backupNames.add(bcb.site())) {
            throw CONFIG.multipleSitesWithSameName(bcb.site());
         }
         bcb.validate();
      }

      //we have backups configured. check if we have a clustered cache
      if (!backupNames.isEmpty() && !builder.clustering().cacheMode().isClustered()) {
         throw CONFIG.xsiteInLocalCache();
      }

      if (attributes.attribute(MERGE_POLICY).get() == null) {
         throw CONFIG.missingXSiteEntryMergePolicy();
      }

      for (String site : attributes.attribute(IN_USE_BACKUP_SITES).get()) {
         boolean found = false;
         for (BackupConfigurationBuilder bcb : backups) {
            if (bcb.site().equals(site)) found = true;
         }
         if (!found) {
            throw CONFIG.siteMustBeInBackups(site);
         }
      }
   }

   @Override
   public void validate(GlobalConfiguration globalConfig) {
      backupForBuilder.validate(globalConfig);

      for (BackupConfigurationBuilder bcb : backups) {
         bcb.validate(globalConfig);
      }
   }

   @Override
   public SitesConfiguration create() {
      List<BackupConfiguration> backupConfigurations = new ArrayList<>(backups.size());
      for (BackupConfigurationBuilder bcb : this.backups) {
         backupConfigurations.add(bcb.create());
      }
      return new SitesConfiguration(attributes.protect(), backupConfigurations, backupForBuilder.create());
   }

   @Override
   public SitesConfigurationBuilder read(SitesConfiguration template) {
      this.attributes.read(template.attributes());
      backupForBuilder.read(template.backupFor());
      //backups.clear();
      for (BackupConfiguration bc : template.allBackups()) {
         BackupConfigurationBuilder bcb = new BackupConfigurationBuilder(getBuilder());
         bcb.read(bc);
         backups.add(bcb);
      }
      return this;
   }

}
