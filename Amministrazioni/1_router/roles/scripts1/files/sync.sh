#!/bin/bash

# 1. Rilevamento IP locale e impostazione del Server Remoto
IP=$(ip a show eth1 | grep -w "inet" | awk '{print $2}' | cut -d/ -f1)
SERVER=172.20.20.1

if [[ "$IP" == "$SERVER" ]]; then
        SERVER=172.20.20.2
fi      

# 2. Ciclo sugli utenti estratti da LDAP che hanno description = $SERVER
for USER in $(ldapsearch -x -H ldap://172.20.20.6/ -LLL -b dc=labammsis "(description=$SERVER)" uid | grep -v dn | awk -F ': ' '{print $2}')
do
        # A. Imposta description a 'busy'
        ldapmodify -x -H ldap://172.20.20.6/ -D "cn=admin,dc=labammsis" -w "gennaio.marzo" <<EOF
dn: uid=$USER,ou=People,dc=labammsis
changetype: modify
replace: description
description: busy
EOF

        # B. TRASFERIMENTO SCP (Soluzione corretta per la tua consegna)
        # Eseguiamo scp impersonando l'utente stesso (usando su -c) per preservare i permessi dei file.
        # StrictHostKeyChecking=no evita il blocco dello script per la richiesta della chiave SSH.
        HOMEDIR="/home/$USER"
	su -s /bin/bash -c "scp -o StrictHostKeyChecking=no -r $USER@$SERVER:'$HOMEDIR/*' $HOMEDIR/" - $USER

        # C. Imposta description a 'sync' a trasferimento ultimato
        ldapmodify -x -H ldap://172.20.20.6/ -D "cn=admin,dc=labammsis" -w "gennaio.marzo" <<EOF
dn: uid=$USER,ou=People,dc=labammsis
changetype: modify
replace: description
description: sync
EOF

done



