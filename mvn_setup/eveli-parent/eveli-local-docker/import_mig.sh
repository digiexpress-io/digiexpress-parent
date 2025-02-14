docker exec -i eveli-local-docker-postgresqlmigfrom-1 /bin/bash -c "PGPASSWORD=password123 psql --username mig-data mig-data" < mig_data/1_assets.sql
docker exec -i eveli-local-docker-postgresqlmigfrom-1 /bin/bash -c "PGPASSWORD=password123 psql --username mig-data mig-data" < mig_data/2_backend.sql
docker exec -i eveli-local-docker-postgresqlmigfrom-1 /bin/bash -c "PGPASSWORD=password123 psql --username mig-data mig-data" < mig_data/3_dialob.sql
docker exec -i eveli-local-docker-postgresqlmigfrom-1 /bin/bash -c "PGPASSWORD=password123 psql --username mig-data mig-data" < mig_data/4_tasks.sql