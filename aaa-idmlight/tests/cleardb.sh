sudo service idmlight stop 
echo "dropping all tables..."
sleep 3
sudo sqlite3 /opt/idmlight/dmlight.db < ../sql/idmlight.sql
sudo service idmlight start
