#!/bin/bash

set -e

echo "🔧 [1/5] 시스템 파일 디스크립터 설정 (/etc/security/limits.conf)..."
LIMITS_CONF="/etc/security/limits.conf"
grep -q 'nofile' $LIMITS_CONF || cat <<EOF >> $LIMITS_CONF
* soft nofile 100000
* hard nofile 100000
EOF

echo "🔧 [2/5] 시스템 커널 파라미터 설정 (/etc/sysctl.conf)..."
SYSCTL_CONF="/etc/sysctl.conf"
grep -q 'vm.overcommit_memory' $SYSCTL_CONF || echo "vm.overcommit_memory = 1" >> $SYSCTL_CONF
grep -q 'net.core.somaxconn' $SYSCTL_CONF || echo "net.core.somaxconn = 1024" >> $SYSCTL_CONF
grep -q 'net.ipv4.tcp_max_syn_backlog' $SYSCTL_CONF || echo "net.ipv4.tcp_max_syn_backlog = 1024" >> $SYSCTL_CONF

sysctl -p

echo "🔧 [3/5] Transparent Huge Pages 비활성화..."
echo never > /sys/kernel/mm/transparent_hugepage/enabled
echo never > /sys/kernel/mm/transparent_hugepage/defrag

echo "🔧 [4/5] rc.local 등록 (부팅 시 THP off)..."
RC_LOCAL="/etc/rc.local"
if [ ! -f "$RC_LOCAL" ]; then
  echo '#!/bin/bash' > $RC_LOCAL
  chmod +x $RC_LOCAL
fi

grep -q 'transparent_hugepage/enabled' $RC_LOCAL || cat <<EOF >> $RC_LOCAL
if test -f /sys/kernel/mm/transparent_hugepage/enabled; then
  echo never > /sys/kernel/mm/transparent_hugepage/enabled
  echo never > /sys/kernel/mm/transparent_hugepage/defrag
fi
EOF

echo "✅ 시스템 튜닝 완료"