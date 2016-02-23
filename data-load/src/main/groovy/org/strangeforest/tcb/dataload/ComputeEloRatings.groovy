package org.strangeforest.tcb.dataload
def sqlPool = SqlPool.create()

def eloRatings = new EloRatings(sqlPool)
def i = 0
eloRatings.compute()
//eloRatings.current().each { printf '%1$4s %2$-30s %3$4s%n', ++i, it.key, it.value }
eloRatings.allTime().each { printf '%1$4s %2$-30s %3$4s%n', ++i, it.key, it.value.bestRating }


