/*
 * Copyright 2014 Codenvy, S.A.
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
package com.codenvy.ide.ext.datasource.client;

import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.ui.action.ActionManager;
import com.codenvy.ide.api.ui.action.Constraints;
import com.codenvy.ide.api.ui.action.DefaultActionGroup;
import com.codenvy.ide.api.ui.keybinding.KeyBindingAgent;
import com.codenvy.ide.api.ui.keybinding.KeyBuilder;
import com.codenvy.ide.api.ui.wizard.DefaultWizard;
import com.codenvy.ide.api.ui.workspace.PartStackType;
import com.codenvy.ide.api.ui.workspace.WorkspaceAgent;
import com.codenvy.ide.collections.Array;
import com.codenvy.ide.collections.Collections;
import com.codenvy.ide.ext.datasource.client.common.CellTableResources;
import com.codenvy.ide.ext.datasource.client.explorer.DatasourceExplorerPartPresenter;
import com.codenvy.ide.ext.datasource.client.newdatasource.NewDatasourceAction;
import com.codenvy.ide.ext.datasource.client.newdatasource.NewDatasourceWizardPagePresenter;
import com.codenvy.ide.ext.datasource.client.newdatasource.NewDatasourceWizardQualifier;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.AbstractNewDatasourceConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.NewDatasourceConnectorAgent;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.amazon.ws.mysql.AwsMysqlConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.amazon.ws.oracle.AwsOracleConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.amazon.ws.postgres.AwsPostgresConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.amazon.ws.sqlserver.AwsSqlServerConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.google.cloud.sql.GoogleCloudSqlConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.mssqlserver.MssqlserverDatasourceConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.mysql.MysqlDatasourceConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.nuodb.NuoDBDatasourceConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.oracle.OracleDatasourceConnectorPage;
import com.codenvy.ide.ext.datasource.client.newdatasource.connector.postgres.PostgresDatasourceConnectorPage;
import com.codenvy.ide.ext.datasource.client.sqllauncher.ExecuteSqlAction;
import com.codenvy.ide.util.input.CharCodeWithModifiers;
import com.codenvy.ide.util.input.KeyCodeMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import static com.codenvy.ide.api.ui.action.Anchor.BEFORE;
import static com.codenvy.ide.api.ui.action.IdeActions.GROUP_MAIN_MENU;
import static com.codenvy.ide.api.ui.action.IdeActions.GROUP_WINDOW;
import static com.codenvy.ide.ext.datasource.client.DatabaseCategoryType.CLOUD;
import static com.codenvy.ide.ext.datasource.client.DatabaseCategoryType.NOTCLOUD;

/**
 * Extension definition for the datasource plugin.
 */
@Singleton
@Extension(title = "Datasource Extension", version = "1.0.0")
public class DatasourceExtension {

    public static boolean SHOW_ITEM                  = true;
    public static String  DS_GROUP_MAIN_MENU         = "DatasourceMainMenu";

    private static String DS_ACTION_SHORTCUT_EXECUTE = "DatasourceActionExecute";

    @Inject
    public DatasourceExtension(WorkspaceAgent workspaceAgent,
                               DatasourceExplorerPartPresenter dsExplorer,
                               ActionManager actionManager,
                               NewDatasourceAction newDSConnectionAction,
                               Provider<NewDatasourceWizardPagePresenter> newDatasourcePageProvider,
                               @NewDatasourceWizardQualifier DefaultWizard wizard,
                               NewDatasourceConnectorAgent connectorAgent,
                               DatasourceUiResources resources,
                               CellTableResources celltableResources,
                               Provider<PostgresDatasourceConnectorPage> pgConnectorPageProvider,
                               Provider<MysqlDatasourceConnectorPage> mysqlConnectorPageProvider,
                               Provider<OracleDatasourceConnectorPage> oracleConnectorPageProvider,
                               Provider<MssqlserverDatasourceConnectorPage> mssqlserverConnectorPageProvider,
                               Provider<NuoDBDatasourceConnectorPage> nuodbConnectorPageProvider,
                               Provider<GoogleCloudSqlConnectorPage> googleCloudSqlConnectorPageProvider,
                               Provider<AwsPostgresConnectorPage> awsPostgresConnectorPageProvider ,
                               Provider<AwsMysqlConnectorPage> awsMysqlConnectorPageProvider,
                               Provider<AwsOracleConnectorPage> awsOracleConnectorPageProvider,
                               Provider<AwsSqlServerConnectorPage> awsSqlServerConnectorPageProvider,
                               AvailableJdbcDriversService availableJdbcDrivers,
                               ExecuteSqlAction executeSqlAction,
                               KeyBindingAgent keyBindingAgent) {

        workspaceAgent.openPart(dsExplorer, PartStackType.NAVIGATION);

        // create de "Datasource" menu in menubar and insert it
        DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);
        DefaultActionGroup defaultDatasourceMainGroup = new DefaultActionGroup("Datasource", true, actionManager);
        actionManager.registerAction(DS_GROUP_MAIN_MENU, defaultDatasourceMainGroup);
        Constraints beforeWindow = new Constraints(BEFORE, GROUP_WINDOW);
        mainMenu.add(defaultDatasourceMainGroup, beforeWindow);

        // add submenu "New datasource" to Datasource menu
        actionManager.registerAction("NewDSConnection", newDSConnectionAction);
        defaultDatasourceMainGroup.add(newDSConnectionAction);

        wizard.addPage(newDatasourcePageProvider);

        // fetching available drivers list from the server
        availableJdbcDrivers.fetch();

        // inject CSS
        resources.datasourceUiCSS().ensureInjected();
        celltableResources.cellTableStyle().ensureInjected();

        // counter to add different priorities to all connectors - to increment after each #register(NewDatasourceConnector)
        int connectorCounter = 0;

        // add a new postgres connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> pgWizardPages = Collections.createArray();
        pgWizardPages.add(pgConnectorPageProvider);
        connectorAgent.register(PostgresDatasourceConnectorPage.PG_DB_ID, connectorCounter, "PostgreSQL",
                                resources.getPostgreSqlLogo(), "org.postgresql.Driver", pgWizardPages, NOTCLOUD);

        connectorCounter++;

        // Add a new mysql connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> mysqlWizardPages = Collections.createArray();
        mysqlWizardPages.add(mysqlConnectorPageProvider);
        connectorAgent.register(MysqlDatasourceConnectorPage.MYSQL_DB_ID, connectorCounter, "MySQL",
                                resources.getMySqlLogo(), "com.mysql.jdbc.Driver", mysqlWizardPages, NOTCLOUD);

        connectorCounter++;

        // add a new oracle connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> oracleWizardPages = Collections.createArray();
        oracleWizardPages.add(oracleConnectorPageProvider);
        connectorAgent.register(OracleDatasourceConnectorPage.ORACLE_DB_ID, connectorCounter,
                                "Oracle", resources.getOracleLogo(), "oracle.jdbc.OracleDriver", oracleWizardPages, NOTCLOUD);

        connectorCounter++;

        // add a new SQLserver connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> sqlServerWizardPages = Collections.createArray();
        sqlServerWizardPages.add(mssqlserverConnectorPageProvider);
        connectorAgent.register(MssqlserverDatasourceConnectorPage.SQLSERVER_DB_ID, connectorCounter,
                                "MsSqlServer", resources.getSqlServerLogo(), "net.sourceforge.jtds.jdbc.Driver", sqlServerWizardPages, NOTCLOUD);

        connectorCounter++;

        // add a new NuoDB connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> nuoDBWizardPages = Collections.createArray();
        nuoDBWizardPages.add(nuodbConnectorPageProvider);
        connectorAgent.register(NuoDBDatasourceConnectorPage.NUODB_DB_ID, connectorCounter,
                                "NuoDB", resources.getNuoDBLogo(), "com.nuodb.jdbc.Driver", nuoDBWizardPages, NOTCLOUD);
        connectorCounter++;

        // add a new GoogleCloudSQL connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> googleCloudSQLWizardPages = Collections.createArray();
        googleCloudSQLWizardPages.add(googleCloudSqlConnectorPageProvider);
        connectorAgent.register(GoogleCloudSqlConnectorPage.GOOGLECLOUDSQL_DB_ID, connectorCounter,
                "GoogleCloudSQL", resources.getGoogleCloudSQLLogo(), "com.mysql.jdbc.Driver", googleCloudSQLWizardPages, CLOUD);

        connectorCounter++;

        // add a new AmazonRDS/Postgres connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> awsPostgresWizardPages = Collections.createArray();
        awsPostgresWizardPages.add(awsPostgresConnectorPageProvider);
        connectorAgent.register(AwsPostgresConnectorPage.AWSPOSTGRES_DB_ID, connectorCounter,
                "Aws/Postgres", resources.getAwsPostgresLogo(), "org.postgresql.Driver", awsPostgresWizardPages, CLOUD);

        connectorCounter++;

        // add a new AmazonRDS/Mysql connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> awsMysqlWizardPages = Collections.createArray();
        awsMysqlWizardPages.add(awsMysqlConnectorPageProvider);
        connectorAgent.register(AwsMysqlConnectorPage.AWSMYSQL_DB_ID, connectorCounter,
                "Aws/Mysql", resources.getAwsMysqlLogo(), "com.mysql.jdbc.Driver", awsMysqlWizardPages, CLOUD);

        connectorCounter++;

        // add a new AmazonRDS/Oracle connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> awsOracleWizardPages = Collections.createArray();
        awsOracleWizardPages.add(awsOracleConnectorPageProvider);
        connectorAgent.register(AwsOracleConnectorPage.AWSORACLE_DB_ID, connectorCounter,
                "Aws/Oracle", resources.getAwsOracleLogo(), "oracle.jdbc.OracleDriver", awsOracleWizardPages, CLOUD);

        connectorCounter++;

        // add a new AmazonRDS/SqlServer connector
        Array<Provider< ? extends AbstractNewDatasourceConnectorPage>> awsSqlServerWizardPages = Collections.createArray();
        awsSqlServerWizardPages.add(awsSqlServerConnectorPageProvider);
        connectorAgent.register(AwsSqlServerConnectorPage.AWSSQLSERVER_DB_ID, connectorCounter,
                "Aws/SqlServer", resources.getAwsSqlServerLogo(), "net.sourceforge.jtds.jdbc.Driver", awsSqlServerWizardPages, CLOUD);

        connectorCounter++;


        // Add execute shortcut
        actionManager.registerAction(DS_ACTION_SHORTCUT_EXECUTE, executeSqlAction);
        final CharCodeWithModifiers key = new KeyBuilder().action().charCode(KeyCodeMap.ENTER).build();
        keyBindingAgent.getGlobal().addKey(key, DS_ACTION_SHORTCUT_EXECUTE);
    }
}