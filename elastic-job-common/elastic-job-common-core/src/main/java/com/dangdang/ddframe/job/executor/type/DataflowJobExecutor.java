/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.job.executor.type;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.executor.AbstractElasticJobExecutor;
import com.dangdang.ddframe.job.executor.JobFacade;

import java.util.List;

/**
 * 数据流作业执行器.
 * 
 * @author zhangliang
 */
public final class DataflowJobExecutor extends AbstractElasticJobExecutor {

    /**
     * 数据流作业对象
     */
    private final DataflowJob<Object> dataflowJob;
    
    public DataflowJobExecutor(final DataflowJob<Object> dataflowJob, final JobFacade jobFacade) {
        super(jobFacade);
        this.dataflowJob = dataflowJob;
    }
    
    @Override
    protected void process(final ShardingContext shardingContext) {
        DataflowJobConfiguration dataflowConfig = (DataflowJobConfiguration) getJobRootConfig().getTypeConfig();
        if (dataflowConfig.isStreamingProcess()) { // 流式处理数据
            streamingExecute(shardingContext);
        } else {
            oneOffExecute(shardingContext);
        }
    }

    /**
     * 流式处理
     *
     * @param shardingContext 分片上下文
     */
    private void streamingExecute(final ShardingContext shardingContext) {
        List<Object> data = fetchData(shardingContext);
        while (null != data && !data.isEmpty()) {
            processData(shardingContext, data);
            if (!getJobFacade().isEligibleForJobRunning()) {
                break;
            }
            data = fetchData(shardingContext);
        }
    }

    /**
     * 一次处理
     *
     * @param shardingContext 分片上下文
     */
    private void oneOffExecute(final ShardingContext shardingContext) {
        List<Object> data = fetchData(shardingContext);
        if (null != data && !data.isEmpty()) {
            processData(shardingContext, data);
        }
    }

    /**
     * 加载数据
     *
     * @param shardingContext 分片上下文
     * @return 数据
     */
    private List<Object> fetchData(final ShardingContext shardingContext) {
        return dataflowJob.fetchData(shardingContext);
    }

    /**
     * 处理数据
     *
     * @param shardingContext 分片上下文
     * @param data 数据
     */
    private void processData(final ShardingContext shardingContext, final List<Object> data) {
        dataflowJob.processData(shardingContext, data);
    }
}
