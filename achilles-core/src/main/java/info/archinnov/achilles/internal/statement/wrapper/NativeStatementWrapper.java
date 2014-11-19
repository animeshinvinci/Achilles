/*
 * Copyright (C) 2012-2014 DuyHai DOAN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package info.archinnov.achilles.internal.statement.wrapper;



import com.datastax.driver.core.*;
import info.archinnov.achilles.internal.statement.StatementHelper;
import org.apache.commons.lang3.ArrayUtils;
import com.google.common.base.Optional;
import info.archinnov.achilles.listener.CASResultListener;

public class NativeStatementWrapper extends AbstractStatementWrapper {


    private Statement statement;

    public NativeStatementWrapper(Class<?> entityClass, Statement statement, Object[] values, Optional<CASResultListener> casResultListener) {
        super(entityClass, values);
        this.statement = statement;
        super.casResultListener = casResultListener;
    }

    public String getQueryString() {
        return StatementHelper.maybeGetQueryString(statement);
    }


    @Override
    public ResultSet execute(Session session) {
        logDMLStatement("");
        activateQueryTracing(statement);
        ResultSet resultSet;
        if (ArrayUtils.isNotEmpty(super.values)) {
            resultSet = session.execute(getQueryString(), super.values);
        } else {
            resultSet = session.execute(statement);
        }

        tracing(resultSet);
        checkForCASSuccess(getQueryString(), resultSet);
        return resultSet;
    }

    @Override
    public Statement getStatement() {
        if (statement instanceof RegularStatement) {
            return buildParameterizedStatement();
        } else {
            return statement;
        }
    }

    @Override
    public void logDMLStatement(String indentation) {
        if (dmlLogger.isDebugEnabled() || displayDMLForEntity) {
            String queryType = statement.getClass().getSimpleName();
            String queryString = getQueryString();
            String consistencyLevel = statement.getConsistencyLevel() == null ? "DEFAULT" : statement
                    .getConsistencyLevel().name();
            writeDMLStatementLog(queryType, queryString, consistencyLevel, values);
        }
    }

    public Statement buildParameterizedStatement() {
        if (statement instanceof RegularStatement) {
            final RegularStatement regularStatement = (RegularStatement) statement;
            if (ArrayUtils.isEmpty(regularStatement.getValues()) && ArrayUtils.isNotEmpty(values)) {
                final SimpleStatement statement = new SimpleStatement(getQueryString(), values);

                if (this.statement.getConsistencyLevel() != null) {
                    statement.setConsistencyLevel(this.statement.getConsistencyLevel());
                }

                if (this.statement.getSerialConsistencyLevel() != null) {
                    statement.setSerialConsistencyLevel(this.statement.getSerialConsistencyLevel());
                }
                return statement;
            } else {
                return statement;
            }
        } else {
            return statement;
        }

    }
}
