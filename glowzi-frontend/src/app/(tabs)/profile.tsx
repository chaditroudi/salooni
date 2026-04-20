import React, { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useAuth } from '@/contexts/auth-context';
import { useTheme } from '@/hooks/use-theme';

export default function ProfileScreen() {
  const { user, logout } = useAuth();
  const theme = useTheme();
  const [loggingOut, setLoggingOut] = useState(false);

  const handleLogout = async () => {
    setLoggingOut(true);
    try {
      await logout();
    } catch {
      Alert.alert('Error', 'Failed to log out. Please try again.');
    } finally {
      setLoggingOut(false);
    }
  };

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <ScrollView contentContainerStyle={styles.scroll}>
          <View style={styles.content}>
            <View style={styles.avatarCircle}>
              <ThemedText style={styles.avatarText}>
                {user?.fullName?.charAt(0)?.toUpperCase() ?? '?'}
              </ThemedText>
            </View>

            <ThemedText type="subtitle" style={styles.name}>
              {user?.fullName ?? 'Unknown'}
            </ThemedText>
            <ThemedText type="small" themeColor="textSecondary">
              {user?.role}
            </ThemedText>

            <ThemedView type="backgroundElement" style={styles.infoCard}>
              <InfoRow label="Phone" value={user?.phone ?? '—'} theme={theme} />
              <View style={[styles.divider, { backgroundColor: theme.backgroundSelected }]} />
              <InfoRow label="Role" value={user?.role ?? '—'} theme={theme} />
              <View style={[styles.divider, { backgroundColor: theme.backgroundSelected }]} />
              <InfoRow label="Language" value={user?.preferredLanguage ?? 'en'} theme={theme} />
            </ThemedView>

            <Pressable
              style={[styles.logoutButton, loggingOut && styles.buttonDisabled]}
              onPress={handleLogout}
              disabled={loggingOut}>
              {loggingOut ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <ThemedText style={styles.logoutText}>Sign Out</ThemedText>
              )}
            </Pressable>
          </View>
        </ScrollView>
      </SafeAreaView>
    </ThemedView>
  );
}

function InfoRow({
  label,
  value,
  theme,
}: {
  label: string;
  value: string;
  theme: Record<string, string>;
}) {
  return (
    <View style={styles.infoRow}>
      <ThemedText type="small" themeColor="textSecondary">
        {label}
      </ThemedText>
      <ThemedText type="small">{value}</ThemedText>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  safeArea: { flex: 1 },
  scroll: {
    flexGrow: 1,
    padding: Spacing.four,
    paddingBottom: BottomTabInset + Spacing.four,
  },
  content: {
    alignItems: 'center',
    maxWidth: MaxContentWidth,
    width: '100%',
    alignSelf: 'center',
  },
  avatarCircle: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#3c87f7',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: Spacing.three,
    marginTop: Spacing.five,
  },
  avatarText: {
    color: '#fff',
    fontSize: 32,
    fontWeight: '700',
  },
  name: {
    marginBottom: Spacing.one,
  },
  infoCard: {
    width: '100%',
    borderRadius: 16,
    padding: Spacing.three,
    marginTop: Spacing.five,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: Spacing.two,
  },
  divider: {
    height: 1,
  },
  logoutButton: {
    backgroundColor: '#e53935',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
    width: '100%',
    marginTop: Spacing.five,
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  logoutText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 16,
  },
});
