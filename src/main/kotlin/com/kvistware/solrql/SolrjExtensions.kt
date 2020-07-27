/**
 * Copyright © 2020 KvistWare
 */
package com.kvistware.solrql

import org.apache.solr.client.solrj.SolrQuery

// Query creation
/**
 * The entry function to the query DSL. This call will create a new [SolrQuery] instance with an optional initial query
 * and then apply the given operation block on the instance. This block can be used to conveniently run the functions
 * below for an elegant experience in creating queries.
 *
 * @param q The initial query
 * @param op A lambda that will be applied on the created [SolrQuery]
 *
 * @return The created query after the operation has completed
 */
fun q(q: String = "*:*", op: SolrQuery.() -> Unit): SolrQuery = SolrQuery(q).apply(op)

/**
 * Wraps the [SolrQuery.setFilterQueries] method for convenient DSL calls
 *
 * @param fq The filter queries to apply
 *
 * @return The [SolrQuery] as returned by the wrapped method
 */
@SolrQl
fun SolrQuery.fq(vararg fq: String): SolrQuery = setFilterQueries(*fq)
@SolrQl
fun SolrQuery.addFq(fq: String): SolrQuery = addFilterQuery(fq)

/**
 * Wraps the [SolrQuery.setFields] method for convenient DSL calls
 *
 * @param fl The field list to set
 *
 * @return The [SolrQuery] as returned by the wrapped method
 */
@SolrQl
fun SolrQuery.fl(vararg fl: String): SolrQuery = setFields(*fl)
@SolrQl
fun SolrQuery.addFl(fl: String): SolrQuery = addField(fl)

/**
 * Wraps the creation and adding of an ascending sort clause to the query
 *
 * @param field The field to use for sorting
 *
 * @return The [SolrQuery] as returned by the wrapped method
 */
@SolrQl
fun SolrQuery.sortAsc(field: String): SolrQuery = addSort(SolrQuery.SortClause.asc(field))

/**
 * Wraps the creation and adding of a descending sort clause to the query
 *
 * @param field The field to use for sorting
 *
 * @return The [SolrQuery] as returned by the wrapped method
 */
@SolrQl
fun SolrQuery.sortDesc(field: String): SolrQuery = addSort(SolrQuery.SortClause.desc(field))

/**
 * Enables the facet parameter on the query and applies the passed operation. This block should be used to call the
 * additional faceting functions
 *
 * @param op The operation to perform on the query after enabling faceting
 *
 * @return The [SolrQuery] after calling [SolrQuery.setFacet] and applying the [op] function
 *
 * @see SolrQuery.facetField
 * @see SolrQuery.facetQuery
 */
@SolrQl
fun SolrQuery.facet(op: SolrQuery.() -> Unit): SolrQuery = setFacet(true).apply(op)

/**
 * Adds the given [fields] to the query's faceting configuration
 *
 * @param fields One or more fields to get faceting from
 *
 * @return The [SolrQuery] as returned by the wrapped method
 */
@SolrQl
fun SolrQuery.facetField(vararg fields: String): SolrQuery = addFacetField(*fields)

/**
 * Adds the given [query] to the faceting queries
 *
 * @param query The facet query to add
 *
 * @return The [SolrQuery] as returned by the wrapped method
 */
@SolrQl
fun SolrQuery.facetQuery(query: String): SolrQuery = addFacetQuery(query)

@SolrQl
fun SolrQuery.facetLimit(limit: Int): SolrQuery = setFacetLimit(limit)
@SolrQl
fun SolrQuery.facetMinCount(count: Int): SolrQuery = setFacetMinCount(count)


// Clauses
/**
 * Creates a Solr where-clause in the form of "[field]:[value]"
 */
@SolrQl
fun where(field: String, value: Any): String = "$field:$value"

/**
 * Creates a Solr not-clause in the form of "-[field]:[value]"
 */
@SolrQl
fun not(field: String, value: Any): String = "-${where(field, value)}"


// Conditions
/**
 * Joins all [values] with the AND keyword and returns the expression in parentheses, ready for use as a condition in
 * [where] and [not].
 *
 * @param values The values to join
 */
@SolrQl
fun and(vararg values: Any) = "(${values.joinToString(" AND ")})"

/**
 * Joins all [values] with the OR keyword and returns the expression in parentheses, ready for use as a condition in
 * [where] and [not].
 *
 * @param values The values to join
 */
@SolrQl
fun or(vararg values: Any) = "(${values.joinToString(" OR ")})"
@SolrQl
fun or(values: Iterable<Any>) = "(${values.joinToString(" OR ")})"

/**
 * Joins the [lower] boundary and the [upper] boundary into a Solr "[$lower TO $upper]" condition
 *
 * @param lower The lower range boundary
 * @param upper The upper range boundary
 */
@SolrQl
fun range(lower: Any, upper: Any) = "[$lower TO $upper]"

/**
 * DSL-friendly function to create a range condition from a [pair]
 *
 * @param pair The [Pair] of values to form a range condition from
 */
@SolrQl
fun range(pair: Pair<Any, Any>) = range(pair.first, pair.second)

@SolrQl
fun tag(name: String, fq: String) = "{!tag=$name}$fq"

@SolrQl
fun ex(name: String, field: String) = "{!ex=$name}$field"


// DSL extensions
/**
 * DSL-friendly extension to create where-clauses from Strings, where the Strings are the field names.
 *
 * For example, the code:
 *
 * q("id" equals 12345) {
 *   fq("field" equals or(15, 20, 25))
 * }
 *
 * will translate into the query "q=id:12345&fq=field:(15+OR+20+OR+25)"
 *
 * @param value The condition for the field
 */
@SolrQl
infix fun String.equals(value: Any): String = where(this, value)

/**
 * DSL-friendly extension to create not-clauses from Strings, where the Strings are the field names.
 *
 * For example, the code:
 *
 * q {
 *   fq("field" notEquals or(15, 20, 25))
 * }
 *
 * will translate into the query "q=*:*&fq=-field:(15+OR+20+OR+25)"
 *
 * @param value The value for the field
 */
@SolrQl
infix fun String.notEquals(value: Any): String = not(this, value)

/**
 * DSL-friendly extension to create range conditions from Strings, where the Strings are the field names.
 *
 * For example, the code:
 *
 * q {
 *   fq("field" equals (1 TO 20))
 * }
 *
 * will translate into the query "q=*:*&fq=field:[1 TO 20]"
 *
 * @param boundaries A [Pair] containing the upper and lower boundaries of the range
 */
@SolrQl
infix fun String.inRange(boundaries: Pair<Any, Any>) = range(boundaries)

/**
 * Utility function to have an object quote itself. Can be safely used with primitives, [String]s and enum
 * classes.
 * Usage with other classes is possible, but may need overriding their toString
 */
fun <T> T.quote(): String = "\"$this\""
