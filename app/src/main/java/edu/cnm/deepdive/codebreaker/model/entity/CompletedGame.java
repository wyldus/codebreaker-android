package edu.cnm.deepdive.codebreaker.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "completed_game")
public class CompletedGame {

  @PrimaryKey
  @ColumnInfo(name = "completed_game_id")
  private long id;

  @ColumnInfo(name = "service_key")
  private String serviceKey;

  private Date started;

  @ColumnInfo(index = true)
  private Date completed;

  @ColumnInfo(index = true)
  private int attempts;

  @ColumnInfo(name = "code_length", index = true)
  private int codeLength;

  @ColumnInfo(name = "pool_size", index = true)
  private int poolSize;
}
