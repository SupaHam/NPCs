package com.supaham.npcs.npcs.database;

import java.util.List;

import com.supaham.npcs.NPCData;

/**
 * Database
 */
public interface Database {

  List<NPCData> findAll();

  void saveAll(List<NPCData> datas);
}
