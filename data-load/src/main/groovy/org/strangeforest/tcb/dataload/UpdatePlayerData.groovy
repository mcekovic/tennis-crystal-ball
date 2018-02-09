package org.strangeforest.tcb.dataload

def loader = new WikipediaPlayerDataLoader(new SqlPool())
loader.updatePlayerData()
