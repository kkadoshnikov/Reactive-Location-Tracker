CREATE KEYSPACE IF NOT EXISTS locations
  WITH REPLICATION = {
   'class' : 'SimpleStrategy',
   'replication_factor' : 1
  };

CREATE TABLE IF NOT EXISTS locations.tracks (
    user_id bigint,
    hr timestamp,
    lat float,
    lon float,
    ts timestamp,
    country_code text,
    postal_code int,
    country text,
    region text,
    city text,
    street text,
    house text,
    PRIMARY KEY ((hr, user_id), ts)
) WITH CLUSTERING ORDER BY (ts DESC)
  AND default_time_to_live = 604800 -- 7 days
  AND gc_grace_seconds = 86400;