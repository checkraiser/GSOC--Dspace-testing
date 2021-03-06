/*
 * CommunityCollectionIntegrationTest.java
 *
 * Copyright (c) 2002-2009, The DSpace Foundation.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the DSpace Foundation nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package org.dspace.content;

import org.databene.contiperf.Required;
import org.databene.contiperf.PerfTest;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.dspace.AbstractIntegrationTest;
import org.dspace.authorize.AuthorizeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * This is an integration test to ensure collections and communities interact properly
 * @author pvillega
 */
// The code below is attached as an example. Performance checks by ContiPerf
// can be applied at method level or at class level. This shows the sintax
// for class-level checks.
//@PerfTest(invocations = 1000, threads = 20)
//@Required(max = 1200, average = 250)
public class CommunityCollectionIntegrationTest extends AbstractIntegrationTest
{
    /** log4j category */
    private static final Logger log = Logger.getLogger(CommunityCollectionIntegrationTest.class);


    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init()
    {
        super.init();
    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    @Override
    public void destroy()
    {
        super.destroy();
    }

    /**
     * Tests the creation of a community collection tree
     */
    @Test
    @PerfTest(invocations = 200, threads = 1)
    @Required(percentile95 = 500, average = 200, throughput = 5)
    public void testCreateTree() throws SQLException, AuthorizeException
    {
        //we create the structure
        context.turnOffAuthorisationSystem();
        Community parent = Community.create(null, context);        
        Community child1 = Community.create(parent, context);
        
        Collection col1 = Collection.create(context);        
        Collection col2 = Collection.create(context);
        
        child1.addCollection(col1);        
        child1.addCollection(col2);
        
        context.restoreAuthSystemState();
        context.commit();
        
        //verify it works as expected
        assertThat("testCreateTree 0", parent.getParentCommunity(), nullValue());
        assertThat("testCreateTree 1", child1.getParentCommunity(), equalTo(parent));
        assertThat("testCreateTree 2", (Community) col1.getParentObject(), equalTo(child1));
        assertThat("testCreateTree 3", (Community) col2.getParentObject(), equalTo(child1));
    }

     /**
      * Tests that count items works as expected
      */
    @Test
    @PerfTest(invocations = 200, threads = 1)
    @Required(percentile95 = 500, average= 333)
    public void testCountItems() throws SQLException, AuthorizeException
    {
        //make it an even number, not to high to reduce time during testing
        int totalitems = 4;

        //we create the structure
        context.turnOffAuthorisationSystem();
        Community parent = Community.create(null, context);
        Community child1 = Community.create(parent, context);

        Collection col1 = Collection.create(context);
        Collection col2 = Collection.create(context);

        child1.addCollection(col1);
        child1.addCollection(col2);

        for(int count = 0; count < totalitems/2; count++)
        {
            Item item1 = Item.create(context);
            item1.setArchived(true);
            item1.update();
            Item item2 = Item.create(context);
            item2.setArchived(true);
            item2.update();

            col1.addItem(item1);            
            col2.addItem(item2);
        }

        context.restoreAuthSystemState();
        context.commit();

        //verify it works as expected
        assertThat("testCountItems 0", col1.countItems(), equalTo(totalitems/2));
        assertThat("testCountItems 1", col2.countItems(), equalTo(totalitems/2));
        assertThat("testCountItems 2", child1.countItems(), equalTo(totalitems));
        assertThat("testCountItems 3", parent.countItems(), equalTo(totalitems));
    }
}
