import { Link } from 'expo-router';
import React, { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  TextInput,
  View,
} from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';
import { useAuth } from '@/contexts/auth-context';
import { useTheme } from '@/hooks/use-theme';
import type { UserRole } from '@/types/auth';

const ROLES: { label: string; value: UserRole }[] = [
  { label: 'Customer', value: 'CUSTOMER' },
  { label: 'Provider', value: 'PROVIDER' },
];

export default function RegisterScreen() {
  const { register } = useAuth();
  const theme = useTheme();

  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('CUSTOMER');
  const [loading, setLoading] = useState(false);

  const handleRegister = async () => {
    if (!fullName.trim() || !phone.trim() || !password.trim()) {
      Alert.alert('Validation', 'Please fill in all fields.');
      return;
    }

    setLoading(true);
    try {
      await register({
        fullName: fullName.trim(),
        phone: phone.trim(),
        password,
        role,
      });
    } catch (err: any) {
      const data = err?.response?.data;
      let message = 'Registration failed.';
      if (data?.fields) {
        message = Object.values(data.fields).join('\n');
      } else if (data?.error) {
        message = data.error;
      }
      Alert.alert('Registration Error', message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ThemedView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.flex}>
        <ScrollView
          contentContainerStyle={styles.scroll}
          keyboardShouldPersistTaps="handled">
          <View style={styles.header}>
            <ThemedText type="title">Glowzi</ThemedText>
            <ThemedText type="subtitle" style={styles.subtitle}>
              Create Account
            </ThemedText>
          </View>

          <View style={styles.form}>
            <ThemedText type="small" style={styles.label}>
              Full Name
            </ThemedText>
            <TextInput
              style={[styles.input, { color: theme.text, borderColor: theme.backgroundSelected }]}
              placeholder="Ahmed Al-Saud"
              placeholderTextColor={theme.textSecondary}
              value={fullName}
              onChangeText={setFullName}
              autoCapitalize="words"
              autoComplete="name"
            />

            <ThemedText type="small" style={styles.label}>
              Phone Number
            </ThemedText>
            <TextInput
              style={[styles.input, { color: theme.text, borderColor: theme.backgroundSelected }]}
              placeholder="+966501234567"
              placeholderTextColor={theme.textSecondary}
              value={phone}
              onChangeText={setPhone}
              keyboardType="phone-pad"
              autoCapitalize="none"
              autoComplete="tel"
            />

            <ThemedText type="small" style={styles.label}>
              Password
            </ThemedText>
            <TextInput
              style={[styles.input, { color: theme.text, borderColor: theme.backgroundSelected }]}
              placeholder="Min 8 chars, 1 upper, 1 lower, 1 digit"
              placeholderTextColor={theme.textSecondary}
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              autoCapitalize="none"
            />

            <ThemedText type="small" style={styles.label}>
              Account Type
            </ThemedText>
            <View style={styles.roleRow}>
              {ROLES.map((r) => (
                <Pressable
                  key={r.value}
                  style={[
                    styles.roleChip,
                    {
                      borderColor: theme.backgroundSelected,
                      backgroundColor:
                        role === r.value ? '#3c87f7' : theme.backgroundElement,
                    },
                  ]}
                  onPress={() => setRole(r.value)}>
                  <ThemedText
                    style={[
                      styles.roleText,
                      { color: role === r.value ? '#fff' : theme.text },
                    ]}>
                    {r.label}
                  </ThemedText>
                </Pressable>
              ))}
            </View>

            <Pressable
              style={[styles.button, loading && styles.buttonDisabled]}
              onPress={handleRegister}
              disabled={loading}>
              {loading ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <ThemedText style={styles.buttonText}>Create Account</ThemedText>
              )}
            </Pressable>

            <View style={styles.footer}>
              <ThemedText type="small" themeColor="textSecondary">
                Already have an account?{' '}
              </ThemedText>
              <Link href="/(auth)/login" asChild>
                <Pressable>
                  <ThemedText type="linkPrimary">Sign In</ThemedText>
                </Pressable>
              </Link>
            </View>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  container: { flex: 1 },
  scroll: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: Spacing.four,
  },
  header: {
    alignItems: 'center',
    marginBottom: Spacing.five,
  },
  subtitle: {
    marginTop: Spacing.two,
  },
  form: {
    width: '100%',
    maxWidth: 400,
    alignSelf: 'center',
  },
  label: {
    marginBottom: Spacing.one,
    marginTop: Spacing.three,
  },
  input: {
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: Spacing.three,
    paddingVertical: Platform.select({ ios: 14, default: 10 }),
    fontSize: 16,
  },
  roleRow: {
    flexDirection: 'row',
    gap: Spacing.two,
  },
  roleChip: {
    flex: 1,
    borderWidth: 1,
    borderRadius: 12,
    paddingVertical: 10,
    alignItems: 'center',
  },
  roleText: {
    fontWeight: '600',
    fontSize: 14,
  },
  button: {
    backgroundColor: '#3c87f7',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
    marginTop: Spacing.four,
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 16,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: Spacing.four,
  },
});
