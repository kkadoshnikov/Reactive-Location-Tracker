build: run migrate_cassandra migrate_grafana

run:
	docker run -d --name cassandra --network=host -v ${ROOT_DIR}/location-tracker-api/src/main/resources/init_db.cql:/etc/cassandra/migration/init_db.cql \
	 -v ${ROOT_DIR}/docker/cassandra-env.sh:/etc/cassandra/cassandra-env.sh cassandra:3.11.4
	docker run -d --name=prometheus --network=host -v ${ROOT_DIR}/docker/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus \
	--config.file=/etc/prometheus/prometheus.yml
	docker run --name redis --network=host -d redis redis-server --appendonly yes
	docker run -d --name=grafana --network=host grafana/grafana
	docker run -d --name=cassandra_exporter -v ${ROOT_DIR}/docker/cassandra_exporter.yml:/etc/cassandra_exporter/config.yml \
	--network=host criteord/cassandra_exporter:latest

start:
	docker start cassandra
	docker start redis
	docker start cassandra_exporter

stop:
	docker stop cassandra || true
	docker stop redis || true
	docker stop cassandra_exporter || true

clean: stop
	docker rm cassandra || true
	docker rm redis || true
	docker rm cassandra_exporter || true

migrate_cassandra:
	for attempt in 1 2 4 6 8 ; do \
		docker exec -it cassandra cqlsh -f /etc/cassandra/migration/init_db.cql && break; \
		sleep $$attempt; \
	done
	
