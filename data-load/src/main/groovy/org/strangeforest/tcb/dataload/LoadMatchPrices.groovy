package org.strangeforest.tcb.dataload

def sqlPool = new SqlPool()

def loader = new ATPTennisLoader()
loader.loadMatchPrices(new MatchPricesLoader(sqlPool))
