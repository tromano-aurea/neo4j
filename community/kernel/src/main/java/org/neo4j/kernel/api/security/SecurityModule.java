/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.api.security;

import org.neo4j.helpers.Service;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.kernel.api.security.provider.SecurityProvider;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.impl.util.DependencySatisfier;
import org.neo4j.kernel.lifecycle.LifeSupport;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.logging.internal.LogService;
import org.neo4j.scheduler.JobScheduler;

public abstract class SecurityModule extends Service implements Lifecycle, SecurityProvider
{
    protected final LifeSupport life = new LifeSupport();

    public SecurityModule( String key, String... altKeys )
    {
        super( key, altKeys );
    }

    public abstract void setup( Dependencies dependencies ) throws KernelException;

    @Override
    public void init() throws Throwable
    {
        life.init();
    }

    @Override
    public void start() throws Throwable
    {
        life.start();
    }

    @Override
    public void stop() throws Throwable
    {
        life.stop();
    }

    @Override
    public void shutdown() throws Throwable
    {
        life.shutdown();
    }

    public interface Dependencies
    {
        LogService logService();

        Config config();

        Procedures procedures();

        JobScheduler scheduler();

        FileSystemAbstraction fileSystem();

        DependencySatisfier dependencySatisfier();
    }
}
