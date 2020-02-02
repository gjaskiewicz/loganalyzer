-- creates table for logs

CREATE TABLE IF NOT EXISTS log_entries (
  id varchar(20), 
  type varchar(20),
  host varchar(20),
  time_start bigint,
  time_end bigint,
  time_delta bigint,
  alert boolean
);