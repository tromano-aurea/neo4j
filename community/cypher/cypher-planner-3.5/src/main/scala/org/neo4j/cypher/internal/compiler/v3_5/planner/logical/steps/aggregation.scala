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
package org.neo4j.cypher.internal.compiler.v3_5.planner.logical.steps

import org.neo4j.cypher.internal.compiler.v3_5.planner.logical.LogicalPlanningContext
import org.neo4j.cypher.internal.compiler.v3_5.planner.logical.steps.replacePropertyLookupsWithVariables.firstAs
import org.neo4j.cypher.internal.ir.v3_5.AggregatingQueryProjection
import org.neo4j.cypher.internal.planner.v3_5.spi.PlanningAttributes.{Cardinalities, Solveds}
import org.neo4j.cypher.internal.v3_5.logical.plans.LogicalPlan

object aggregation {
  def apply(plan: LogicalPlan, aggregation: AggregatingQueryProjection, context: LogicalPlanningContext, solveds: Solveds, cardinalities: Cardinalities): (LogicalPlan, LogicalPlanningContext) = {

    // We want to leverage if we got the value from an index already
    val (aggregationWithRenames, newSemanticTable) = firstAs[AggregatingQueryProjection](replacePropertyLookupsWithVariables(plan.availableCachedNodeProperties)(aggregation, context.semanticTable))
    val newContext = context.withUpdatedSemanticTable(newSemanticTable)

    val expressionSolver = PatternExpressionSolver()
    val (step1, groupingExpressions) = expressionSolver(plan, aggregationWithRenames.groupingExpressions, newContext, solveds, cardinalities)
    val (rewrittenPlan, aggregations) = expressionSolver(step1, aggregationWithRenames.aggregationExpressions, newContext, solveds, cardinalities)

    val finalPlan = newContext.logicalPlanProducer.planAggregation(
      rewrittenPlan,
      groupingExpressions,
      aggregations,
      aggregation.groupingExpressions,
      aggregation.aggregationExpressions,
      newContext)
    (finalPlan, newContext)
  }
}
