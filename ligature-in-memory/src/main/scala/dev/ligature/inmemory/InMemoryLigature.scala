/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.inmemory

import dev.ligature.{Graph, Label, Ligature, LigatureError, QueryTx, Edge, Value}
import scala.collection.mutable.{Set, TreeMap}

protected case class GraphStore(var counter: Long = 0, val edges: Set[Edge] = Set())

final class LigatureInMemory(
    private val store: TreeMap[Graph, GraphStore] = TreeMap[Graph, GraphStore]()
) extends Ligature {
//  private val store = AtomicCell[IO].of(TreeMap[Graph, GraphStore]())

  /** Returns all Graphs in a Ligature instance. */
  override def allGraphs(): Iterator[Graph] = store.keySet.iterator

  /** Check if a given Graph exists. */
  override def graphExists(graph: Graph): Boolean = store.contains(graph)

  /** Returns all Graphs in a Ligature instance that start with the given
    * prefix.
    */
  override def matchGraphsPrefix(prefix: String): Iterator[Graph] =
    store.keys.filter(_.name.startsWith(prefix)).iterator

  /** Returns all Graphs in a Ligature instance that are in a given range
    * (inclusive, exclusive].
    */
  override def matchGraphsRange(
      start: String,
      end: String
  ): Iterator[Graph] =
    store.keys.filter(k => k.name >= start && k.name < end).iterator

  /** Creates a graph with the given name. TODO should probably return its own
    * error type { InvalidGraph, GraphExists, CouldNotCreateGraph }
    */
  override def createGraph(graph: Graph): Unit =
    if !this.store.contains(graph) then this.store += (graph -> GraphStore())

  /** Deletes a graph with the given name. TODO should probably return its own
    * error type { InvalidGraph, CouldNotDeleteGraph }
    */
  override def deleteGraph(graph: Graph): Unit =
    this.store.remove(graph)

  override def allEdges(graph: Graph): Iterator[Edge] =
    this.store.get(graph) match
      case None        => Iterator.empty
      case Some(value) => value.edges.iterator

  /** Initializes a QueryTx TODO should probably return its own error type
    * CouldNotInitializeQueryTx
    */
  override def query[T](graph: Graph)(fn: QueryTx => T): T =
    store.get(graph) match
      case None => ???
      case Some(graphStore) =>
        val tx = InMemoryQueryTx(graphStore)
        fn(tx)

  override def addEdges(graph: Graph, edges: Iterator[Edge]): Unit =
    this.store.get(graph) match {
      case None =>
        this.store.addOne((graph, GraphStore(0, Set.from(edges.toSet))))
      case Some(store) => store.edges.addAll(edges)
    }
    // store.get(graph) match
    //   case None => ???
    //   case Some(graphStore) =>
    //     edges
    //       .fold(graphStore) { (graphStore, edge) =>
    //         ???
    //         //graphStore.copy(edges = graphStore.edges + edge)
    //       }
    //       .map { graphStore =>
    //         store.updated(graph, graphStore)
    //       }

  override def removeEdges(graph: Graph, edges: Iterator[Edge]): Unit =
    this.store.get(graph) match {
      case None        => ???
      case Some(store) => store.edges.subtractAll(edges)
    }
    // store.get(graph) match
    //   case None => ???
    //   case Some(graphStore) =>
    //     edges
    //       .fold(graphStore) { (graphStore, edge) =>
    //         graphStore.copy(edges = graphStore.edges - edge)
    //       }
    //       .map { graphStore =>
    //         store.updated(graph, graphStore)
    //       }

  override def close(): Unit =
    ()
}