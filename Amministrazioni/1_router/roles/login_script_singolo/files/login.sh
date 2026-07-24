#!/bin/bash

if [[ ! -z $(hostname -I | grep 172.20.20) ]]; then
        /bin/bash
elif [[ ! -z $(hostname -I | grep 192.168.200) ]]; then
        echo "rilevato client"
USER=$(whoami)
[[ $(ldapsearch -x -H ldap://192.168.200.126/ -LLL -b dc=labammsis uid="$USER" description | grep description | awk -F ': ' '{print $2} ') != "sync" ]] && exit 0
ldapmodify -x -H ldap://192.168.200.126/ -D "cn=admin,dc=labammsis" -w "gennaio.marzo" <<EOF
dn: uid="$USER",ou=People,dc=labammsis
changetype: modify
replace: description
description: busy
EOF

mkdir -p /home/$USER

# 1. Recupera la memoria totale e disponibile usando la versione 1 di SNMP
total=$(snmpget -v1 -c public -Oqv 172.20.20.1 UCD-SNMP-MIB::memTotalReal.0)
avail=$(snmpget -v1 -c public -Oqv 172.20.20.1 UCD-SNMP-MIB::memAvailReal.0)

# 2. Calcola la percentuale e salvala nella variabile "percent_used"
percent_used1=$(awk -v t="$total" -v a="$avail" 'BEGIN{ printf "%.2f\n", (1 - a/t) * 100 }')

# 3. Ora puoi usare la variabile (ad esempio per stamparla)
echo "Memoria usata: $percent_used%"
# 1. Recupera la memoria totale e disponibile usando la versione 1 di SNMP
total=$(snmpget -v1 -c public -Oqv 172.20.20.2 UCD-SNMP-MIB::memTotalReal.0)
avail=$(snmpget -v1 -c public -Oqv 172.20.20.2 UCD-SNMP-MIB::memAvailReal.0)

# 2. Calcola la percentuale e salvala nella variabile "percent_used"
percent_used2=$(awk -v t="$total" -v a="$avail" 'BEGIN{ printf "%.2f\n", (1 - a/t) * 100 }')
BESTSERVER=172.20.20.1
if [[ "$percent_used1" -gt "$percent_used2"]]; then
	BESTSERVER=172.20.20.2
fi

/bin/bash

[[ $(ldapsearch -x -H ldap://192.168.200.126/ -LLL -b dc=labammsis uid="$USER" description | grep description | awk -F ': ' '{print $2} ') != "sync" ]] && exit 0
ldapmodify -x -H ldap://192.168.200.126/ -D "cn=admin,dc=labammsis" -w "gennaio.marzo" <<EOF
dn: uid="$USER",ou=People,dc=labammsis
changetype: modify
replace: description
description: "$BESTSERVER"
EOF



fi

