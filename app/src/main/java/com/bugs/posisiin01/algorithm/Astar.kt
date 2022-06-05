package com.bugs.posisiin01.algorithm

import com.bugs.posisiin01.model.Sel
import kotlin.math.abs

class Astar(
    private val selSel: Array<Array<Sel>>,
    private val awal: Sel,
    private val tujuan: Sel
) {
    fun cariRute() : MutableList<Sel> {
        val openList = mutableListOf<Sel>()
        val closedList = mutableListOf<Sel>()
        val rute = mutableListOf<Sel>()

        openList.add(awal)

        while (openList.isNotEmpty()) {
            openList.sortedBy { it.f }

            val current = openList[0]

            closedList.add(current)
            openList.removeAt(0)

            // jika tujuan ditemukan, telusuri rute
            if (current.x == tujuan.x && current.y == tujuan.y) {
                tracePath(current, closedList, rute)
                return rute
            }

            val neighborList = mutableListOf<Sel>()
            addNeighbor(selSel, neighborList, current)
            for (n in neighborList) {
                // if n is not in closedlist
                if (!checkExist(n, closedList)) {
                    // if n is not in openList
                    if (!checkExist(n, openList)) {
                        hitungSkor(n, current, tujuan)
                        changeParent(n, current)
                        openList.add(n)
                    } else {
                        betterG(n, current, openList)
                    }
                }
            }
            neighborList.clear()
        }
        return rute
    }

    private fun tracePath(current: Sel, closedList: MutableList<Sel>, rute: MutableList<Sel>) {
        rute.add(current)
        val parent = current.parent
        if (parent != null) {
            tracePath(parent, closedList, rute)
        }
    }

    private fun changeParent(neighbor: Sel, current: Sel) {
        neighbor.parent = current
    }

    private fun hitungSkor(neighbor: Sel, current: Sel, tujuan: Sel) {
        neighbor.g = skorG(neighbor, current)
        neighbor.h = skorH(neighbor, tujuan)
        neighbor.f = skorF(neighbor)
    }

    private fun skorF(neighbor: Sel) : Int {
        return neighbor.g + neighbor.h
    }

    private fun skorH(neighbor: Sel, tujuan: Sel) : Int {
        // manhattan distance
        return (abs(neighbor.x - tujuan.x)*10) + (abs(neighbor.y - tujuan.y)*10)
    }

    private fun skorG(neighbor: Sel, current: Sel) : Int {
        // vertical movement
        if (abs(current.y - neighbor.y) == 1 && abs(current.x - neighbor.x) == 0)
            return current.g + 10
        // horizontal
        if (abs(current.x - neighbor.x) == 1 && abs(current.y - neighbor.y) == 0)
            return current.g + 10
        // diagonal
        if (abs(current.x - neighbor.x) == 1 && abs(current.y - neighbor.y) == 1 )
            return current.g + 14

        return neighbor.g
    }

    private fun checkExist(neighbor: Sel, list: MutableList<Sel>) : Boolean {
        for (sel in list) {
            if (sel.x == neighbor.x && sel.y == neighbor.y)
                return true
        }
        return false
    }

    private fun betterG(neighbor: Sel, current: Sel, list: MutableList<Sel>) {
        for (sel in list) {
            if (sel.x == neighbor.x && sel.y == neighbor.y) {
                // check better G
                val newG = skorG(neighbor, current)
                val oldG = sel.g
                if (newG < oldG) {
                    sel.g = newG
                    changeParent(neighbor, current)
                }
            }
        }
    }

    private fun addNeighbor(selSel: Array<Array<Sel>>, neighbors: MutableList<Sel>, current: Sel) {
        if (!current.top)
            neighbors.add(selSel[current.x][current.y-1])

        //top right
        if (!current.top && !current.right && !selSel[current.x][current.y-1].right && !selSel[current.x+1][current.y].top)
            neighbors.add(selSel[current.x+1][current.y-1])

        // right
        if (!current.right)
            neighbors.add(selSel[current.x+1][current.y])

        // bottom right
        if (!current.right && !current.bottom && !selSel[current.x+1][current.y].bottom && !selSel[current.x][current.y+1].right)
            neighbors.add(selSel[current.x+1][current.y+1])

        // bottom
        if (!current.bottom)
            neighbors.add(selSel[current.x][current.y+1])

        // bottom left
        if (!current.bottom && !current.left && !selSel[current.x-1][current.y].bottom && !selSel[current.x][current.y+1].left )
            neighbors.add(selSel[current.x-1][current.y+1])

        // left
        if (!current.left)
            neighbors.add(selSel[current.x-1][current.y])

        // top left
        if (!current.left && !current.top && !selSel[current.x][current.y-1].left && !selSel[current.x-1][current.y].top)
            neighbors.add(selSel[current.x-1][current.y-1])
    }

}