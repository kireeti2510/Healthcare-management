import { useEffect, useMemo, useState } from 'react'
import './App.css'

const STORAGE_KEY = 'healthcare_ui_db_v2'
const MAX_LOGIN_ATTEMPTS = 5
const LOGIN_LOCK_MINUTES = 2

const ROLE_TABS = {
  PATIENT: ['profile', 'appointments', 'prescriptions'],
  RECEPTIONIST: ['patients', 'appointments'],
  CLINICIAN: ['appointments', 'prescriptions'],
  PHARMACIST: ['prescriptions'],
  CLINIC_ADMIN: ['users', 'patients', 'appointments', 'prescriptions', 'audit'],
}

const TAB_LABELS = {
  auth: 'Auth',
  profile: 'My Profile',
  users: 'Users',
  patients: 'Patients',
  appointments: 'Appointments',
  prescriptions: 'Prescriptions',
  audit: 'Audit',
}

const initialDb = {
  users: [],
  patients: [],
  appointments: [],
  prescriptions: [],
  audit: [],
}

function nowStamp() {
  return new Date().toISOString()
}

function uid() {
  return crypto.randomUUID()
}

function hashPassword(raw) {
  let hash = 2166136261
  for (let i = 0; i < raw.length; i += 1) {
    hash ^= raw.charCodeAt(i)
    hash +=
      (hash << 1) +
      (hash << 4) +
      (hash << 7) +
      (hash << 8) +
      (hash << 24)
  }
  return `h2:${(hash >>> 0).toString(16)}`
}

function matchesPassword(raw, stored) {
  if (!stored) return false
  if (stored.startsWith('h2:')) return hashPassword(raw) === stored
  return raw === stored
}

function roleBadge(role) {
  switch (role) {
    case 'PATIENT':
      return 'Patient'
    case 'CLINICIAN':
      return 'Clinician'
    case 'PHARMACIST':
      return 'Pharmacist'
    case 'RECEPTIONIST':
      return 'Receptionist'
    case 'CLINIC_ADMIN':
      return 'Admin'
    default:
      return role
  }
}

function shortId(value) {
  if (!value) return '-'
  return `${value.slice(0, 8)}...`
}

function defaultTabForRole(role) {
  return ROLE_TABS[role]?.[0] || 'auth'
}

function addAudit(db, action, userId = null) {
  db.audit.unshift({
    id: uid(),
    action,
    userId,
    timestamp: nowStamp(),
  })
}

function seedDb(db) {
  const seedMarker = db.audit.some((a) => a.action === 'SEED_DEMO_DATA_V1')
  if (seedMarker) return db

  const ensureUser = (email, password, role, extras = {}) => {
    const normalized = email.toLowerCase()
    let user = db.users.find((u) => u.email.toLowerCase() === normalized)
    if (!user) {
      user = {
        userId: uid(),
        email: normalized,
        passwordHash: hashPassword(password),
        role,
        ...extras,
      }
      db.users.push(user)
    }
    return user
  }

  const ensurePatient = (userId, dob, insuranceId, allergies) => {
    let patient = db.patients.find((p) => p.userId === userId)
    if (!patient) {
      patient = {
        userId,
        dob,
        insuranceId,
        allergies,
      }
      db.patients.push(patient)
    }
    return patient
  }

  const admin = ensureUser('admin@clinic.com', 'admin123', 'CLINIC_ADMIN')
  const receptionist = ensureUser('reception@clinic.com', 'recep123', 'RECEPTIONIST', {
    staffId: 'REC-101',
    shift: 'Morning',
  })
  const clinicianA = ensureUser('clinician@clinic.com', 'doc123', 'CLINICIAN', {
    licenceNo: 'LIC-1001',
    speciality: 'General Medicine',
  })
  const clinicianB = ensureUser('heartdoc@clinic.com', 'heart123', 'CLINICIAN', {
    licenceNo: 'LIC-2201',
    speciality: 'Cardiology',
  })
  const pharmacist = ensureUser('pharmacist@clinic.com', 'pharma123', 'PHARMACIST', {
    regNo: 'PH-900',
    pharmacy: 'Main Wing Pharmacy',
  })

  const patientUserA = ensureUser('jane.doe@demo.com', 'jane123', 'PATIENT')
  const patientUserB = ensureUser('john.roe@demo.com', 'john123', 'PATIENT')
  const patientUserC = ensureUser('mia.lee@demo.com', 'mia123', 'PATIENT')

  const patientA = ensurePatient(
    patientUserA.userId,
    '1990-05-12',
    'INS-001',
    ['penicillin'],
  )
  const patientB = ensurePatient(
    patientUserB.userId,
    '1985-09-03',
    'INS-177',
    ['ibuprofen'],
  )
  const patientC = ensurePatient(
    patientUserC.userId,
    '2001-02-27',
    'INS-322',
    [],
  )

  const apptAId = uid()
  const apptBId = uid()
  const apptCId = uid()

  db.appointments.push(
    {
      appointmentId: apptAId,
      patientId: patientA.userId,
      clinicianId: clinicianA.userId,
      scheduledAt: '2026-04-20T09:30:00.000Z',
      status: 'PENDING',
    },
    {
      appointmentId: apptBId,
      patientId: patientB.userId,
      clinicianId: clinicianA.userId,
      scheduledAt: '2026-04-21T11:00:00.000Z',
      status: 'CONFIRMED',
    },
    {
      appointmentId: apptCId,
      patientId: patientC.userId,
      clinicianId: clinicianB.userId,
      scheduledAt: '2026-04-22T15:15:00.000Z',
      status: 'PENDING',
    },
  )

  db.prescriptions.push(
    {
      rxId: uid(),
      appointmentId: apptAId,
      status: 'DRAFT',
      issuedAt: null,
      overrideReason: null,
    },
    {
      rxId: uid(),
      appointmentId: apptBId,
      status: 'ISSUED',
      issuedAt: '2026-04-18T08:30:00.000Z',
      overrideReason: 'Cardiac meds prioritized',
    },
    {
      rxId: uid(),
      appointmentId: apptCId,
      status: 'DISPENSED',
      issuedAt: '2026-04-18T07:05:00.000Z',
      overrideReason: null,
    },
  )

  addAudit(db, 'SEED_DEFAULT_USERS', admin.userId)
  addAudit(db, `CREATE_USER:RECEPTIONIST:${receptionist.email}`, admin.userId)
  addAudit(db, `CREATE_USER:CLINICIAN:${clinicianA.email}`, admin.userId)
  addAudit(db, `CREATE_USER:CLINICIAN:${clinicianB.email}`, admin.userId)
  addAudit(db, `CREATE_USER:PHARMACIST:${pharmacist.email}`, admin.userId)
  addAudit(db, `REGISTER_PATIENT:${patientUserA.email}`, patientUserA.userId)
  addAudit(db, `REGISTER_PATIENT:${patientUserB.email}`, patientUserB.userId)
  addAudit(db, `REGISTER_PATIENT:${patientUserC.email}`, patientUserC.userId)
  addAudit(db, `SCHEDULE_APPOINTMENT:${apptAId}`, receptionist.userId)
  addAudit(db, `SCHEDULE_APPOINTMENT:${apptBId}`, receptionist.userId)
  addAudit(db, `SCHEDULE_APPOINTMENT:${apptCId}`, receptionist.userId)
  addAudit(db, 'SEED_DEMO_DATA_V1', admin.userId)

  return db
}

function loadDb() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return seedDb(structuredClone(initialDb))
    const parsed = JSON.parse(raw)
    const hydrated = {
      ...initialDb,
      ...parsed,
      users: parsed.users || [],
      patients: parsed.patients || [],
      appointments: parsed.appointments || [],
      prescriptions: parsed.prescriptions || [],
      audit: parsed.audit || [],
    }
    return seedDb(hydrated)
  } catch {
    return seedDb(structuredClone(initialDb))
  }
}

function App() {
  const [db, setDb] = useState(() => loadDb())
  const [tab, setTab] = useState('auth')
  const [notice, setNotice] = useState({ type: 'ok', text: 'Ready.' })
  const [currentUserId, setCurrentUserId] = useState('')
  const [failedLoginAttempts, setFailedLoginAttempts] = useState({})
  const [loginLockUntil, setLoginLockUntil] = useState({})

  const [registerForm, setRegisterForm] = useState({
    email: '',
    password: '',
    dob: '',
    insuranceId: '',
    allergies: '',
  })

  const [loginForm, setLoginForm] = useState({ email: '', password: '' })

  const [selectedPatientId, setSelectedPatientId] = useState('')
  const [patientEdit, setPatientEdit] = useState({
    userId: '',
    dob: '',
    insuranceId: '',
    allergies: '',
  })

  const [userCreateForm, setUserCreateForm] = useState({
    role: 'CLINICIAN',
    email: '',
    password: '',
    licenceNo: '',
    speciality: '',
    staffId: '',
    shift: '',
    regNo: '',
    pharmacy: '',
    adminLevel: '',
  })

  const [appointmentForm, setAppointmentForm] = useState({
    patientId: '',
    clinicianId: '',
    scheduledAt: '',
  })

  const [rxForm, setRxForm] = useState({
    appointmentId: '',
    overrideReason: '',
  })

  const usersById = useMemo(() => {
    const map = new Map()
    db.users.forEach((u) => map.set(u.userId, u))
    return map
  }, [db.users])

  const patientsById = useMemo(() => {
    const map = new Map()
    db.patients.forEach((p) => map.set(p.userId, p))
    return map
  }, [db.patients])

  const currentUser = currentUserId ? usersById.get(currentUserId) : null
  const currentRole = currentUser?.role || null

  const clinicians = useMemo(
    () => db.users.filter((u) => u.role === 'CLINICIAN'),
    [db.users],
  )

  const patientOptions = useMemo(
    () =>
      db.patients.map((p) => ({
        userId: p.userId,
        email: usersById.get(p.userId)?.email || 'unknown',
      })),
    [db.patients, usersById],
  )

  const rxAppointmentOptions = useMemo(() => {
    let options = db.appointments
    if (currentRole === 'CLINICIAN') {
      options = options.filter(
        (a) =>
          a.clinicianId === currentUserId &&
          a.status !== 'COMPLETED' &&
          a.status !== 'CANCELLED',
      )
    }
    return options
  }, [db.appointments, currentRole, currentUserId])

  const visibleTabs = useMemo(() => {
    if (!currentRole) return ['auth']
    return ROLE_TABS[currentRole] || ['auth']
  }, [currentRole])

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(db))
  }, [db])

  useEffect(() => {
    if (!currentRole) {
      if (tab !== 'auth') setTab('auth')
      return
    }
    if (!visibleTabs.includes(tab)) {
      setTab(defaultTabForRole(currentRole))
    }
  }, [currentRole, tab, visibleTabs])

  function notify(type, text) {
    setNotice({ type, text })
  }

  function copyId(label, value) {
    if (!value) return
    if (!navigator?.clipboard?.writeText) {
      notify('error', 'Clipboard is not supported in this browser.')
      return
    }
    navigator.clipboard
      .writeText(value)
      .then(() => notify('ok', `${label} copied.`))
      .catch(() => notify('error', 'Unable to copy to clipboard.'))
  }

  function can(action) {
    if (!currentRole) return false
    if (currentRole === 'CLINIC_ADMIN') return true

    if (action === 'VIEW_PATIENT_PROFILE') return currentRole === 'PATIENT'
    if (action === 'MANAGE_PATIENTS') return currentRole === 'RECEPTIONIST'
    if (action === 'SCHEDULE_APPOINTMENT') return currentRole === 'RECEPTIONIST'
    if (action === 'CANCEL_APPOINTMENT') return currentRole === 'RECEPTIONIST' || currentRole === 'PATIENT'
    if (action === 'CREATE_PRESCRIPTION') return currentRole === 'CLINICIAN'
    if (action === 'ISSUE_PRESCRIPTION') return currentRole === 'CLINICIAN'
    if (action === 'DISPENSE_PRESCRIPTION') return currentRole === 'PHARMACIST'
    if (action === 'VOID_PRESCRIPTION') return currentRole === 'CLINICIAN'
    return false
  }

  function write(mutator, successText) {
    try {
      setDb((prev) => {
        const next = structuredClone(prev)
        mutator(next)
        return next
      })
      if (successText) notify('ok', successText)
      return true
    } catch (err) {
      notify('error', err.message || 'Operation failed.')
      return false
    }
  }

  function registerPatient(event) {
    event.preventDefault()
    const email = registerForm.email.trim().toLowerCase()
    const password = registerForm.password.trim()

    if (!email || !password) {
      notify('error', 'Email and password are required.')
      return
    }

    if (db.users.some((u) => u.email.toLowerCase() === email)) {
      notify('error', 'This email is already registered.')
      return
    }

    const userId = uid()
    const allergies = registerForm.allergies
      .split(',')
      .map((a) => a.trim())
      .filter(Boolean)

    write((next) => {
      next.users.push({
        userId,
        email,
        passwordHash: hashPassword(password),
        role: 'PATIENT',
      })
      next.patients.push({
        userId,
        dob: registerForm.dob || null,
        insuranceId: registerForm.insuranceId || null,
        allergies,
      })
      addAudit(next, `REGISTER_PATIENT:${email}`, userId)
    }, `Patient registered (${email}).`)

    setRegisterForm({
      email: '',
      password: '',
      dob: '',
      insuranceId: '',
      allergies: '',
    })
  }

  function login(event) {
    event.preventDefault()
    const email = loginForm.email.trim().toLowerCase()
    const rawPassword = loginForm.password
    const now = Date.now()

    const lockUntil = loginLockUntil[email] || 0
    if (lockUntil > now) {
      const leftSeconds = Math.max(1, Math.ceil((lockUntil - now) / 1000))
      notify('error', `Invalid credentials. Too many failed attempts. Try again in ${leftSeconds}s.`)
      write((next) => {
        addAudit(next, `LOGIN_BLOCKED:${email}`, null)
      })
      return
    }

    const user = db.users.find((u) => u.email.toLowerCase() === email)
    const ok = user && matchesPassword(rawPassword, user.passwordHash)

    write((next) => {
      addAudit(next, ok ? 'LOGIN_SUCCESS' : 'LOGIN_FAILED', ok ? user.userId : null)
    })

    if (!ok) {
      const nextCount = (failedLoginAttempts[email] || 0) + 1
      setFailedLoginAttempts((prev) => ({ ...prev, [email]: nextCount }))

      if (nextCount >= MAX_LOGIN_ATTEMPTS) {
        const nextLockUntil = now + LOGIN_LOCK_MINUTES * 60 * 1000
        setLoginLockUntil((prev) => ({ ...prev, [email]: nextLockUntil }))
        setFailedLoginAttempts((prev) => ({ ...prev, [email]: 0 }))
        notify(
          'error',
          `Invalid credentials. Too many failed attempts. Account locked for ${LOGIN_LOCK_MINUTES} minutes.`,
        )
        return
      }

      const left = MAX_LOGIN_ATTEMPTS - nextCount
      notify('error', `Wrong password. Please try again. ${left} attempt(s) left.`)
      return
    }

    setFailedLoginAttempts((prev) => ({ ...prev, [email]: 0 }))
    setLoginLockUntil((prev) => ({ ...prev, [email]: 0 }))
    setCurrentUserId(user.userId)
    setTab(defaultTabForRole(user.role))
    setLoginForm({ email: '', password: '' })
    notify('ok', `Welcome, ${user.email}. Role: ${roleBadge(user.role)}`)
  }

  function logout() {
    if (currentUser) {
      write((next) => {
        addAudit(next, 'LOGOUT', currentUser.userId)
      })
    }
    setCurrentUserId('')
    setTab('auth')
    notify('ok', 'Logged out successfully.')
  }

  function createUserByRole(event) {
    event.preventDefault()
    if (!can('MANAGE_USERS')) {
      notify('error', 'Only admin can create users.')
      return
    }

    const email = userCreateForm.email.trim().toLowerCase()
    const password = userCreateForm.password.trim()
    const role = userCreateForm.role

    if (!email || !password) {
      notify('error', 'Email and password are required.')
      return
    }

    if (db.users.some((u) => u.email.toLowerCase() === email)) {
      notify('error', 'Email already exists.')
      return
    }

    const userId = uid()

    write((next) => {
      const user = {
        userId,
        email,
        passwordHash: hashPassword(password),
        role,
      }

      if (role === 'CLINICIAN') {
        user.licenceNo = userCreateForm.licenceNo || null
        user.speciality = userCreateForm.speciality || null
      } else if (role === 'RECEPTIONIST') {
        user.staffId = userCreateForm.staffId || null
        user.shift = userCreateForm.shift || null
      } else if (role === 'PHARMACIST') {
        user.regNo = userCreateForm.regNo || null
        user.pharmacy = userCreateForm.pharmacy || null
      } else if (role === 'CLINIC_ADMIN') {
        user.adminLevel = userCreateForm.adminLevel || null
      }

      next.users.push(user)
      if (role === 'PATIENT') {
        next.patients.push({
          userId,
          dob: null,
          insuranceId: null,
          allergies: [],
        })
      }
      addAudit(next, `CREATE_USER:${role}:${email}`, currentUser.userId)
    }, `Created ${roleBadge(role)} user (${email}).`)

    setUserCreateForm((prev) => ({
      ...prev,
      email: '',
      password: '',
      licenceNo: '',
      speciality: '',
      staffId: '',
      shift: '',
      regNo: '',
      pharmacy: '',
      adminLevel: '',
    }))
  }

  function getVisibleAppointments() {
    if (!currentRole) return []
    if (currentRole === 'PATIENT') {
      return db.appointments.filter(
        (a) =>
          a.patientId === currentUserId &&
          a.status !== 'COMPLETED' &&
          a.status !== 'CANCELLED',
      )
    }
    if (currentRole === 'CLINICIAN') {
      return db.appointments.filter(
        (a) =>
          a.clinicianId === currentUserId &&
          a.status !== 'COMPLETED' &&
          a.status !== 'CANCELLED',
      )
    }
    if (currentRole === 'RECEPTIONIST' || currentRole === 'CLINIC_ADMIN') {
      return db.appointments
    }
    return []
  }

  function getVisiblePrescriptions() {
    if (!currentRole) return []

    if (currentRole === 'PATIENT') {
      const mine = new Set(
        db.appointments
          .filter((a) => a.patientId === currentUserId)
          .map((a) => a.appointmentId),
      )
      return db.prescriptions.filter((r) => mine.has(r.appointmentId))
    }

    if (currentRole === 'CLINICIAN') {
      const mine = new Set(
        db.appointments
          .filter(
            (a) =>
              a.clinicianId === currentUserId &&
              a.status !== 'COMPLETED' &&
              a.status !== 'CANCELLED',
          )
          .map((a) => a.appointmentId),
      )
      return db.prescriptions.filter((r) => mine.has(r.appointmentId))
    }

    if (currentRole === 'PHARMACIST' || currentRole === 'CLINIC_ADMIN') {
      return db.prescriptions
    }

    return []
  }

  const visibleAppointments = useMemo(() => getVisibleAppointments(), [db.appointments, currentRole, currentUserId])
  const visiblePrescriptions = useMemo(
    () => getVisiblePrescriptions(),
    [db.prescriptions, db.appointments, currentRole, currentUserId],
  )

  const selectedPatient = useMemo(() => {
    if (!can('MANAGE_PATIENTS') && !can('MANAGE_USERS')) return null
    if (!selectedPatientId) return null
    return db.patients.find((p) => p.userId === selectedPatientId) || null
  }, [db.patients, selectedPatientId, currentRole])

  useEffect(() => {
    if (currentRole === 'PATIENT') {
      const mine = patientsById.get(currentUserId)
      if (!mine) return
      setPatientEdit({
        userId: mine.userId,
        dob: mine.dob || '',
        insuranceId: mine.insuranceId || '',
        allergies: (mine.allergies || []).join(', '),
      })
      return
    }

    if (!selectedPatient) return
    setPatientEdit({
      userId: selectedPatient.userId,
      dob: selectedPatient.dob || '',
      insuranceId: selectedPatient.insuranceId || '',
      allergies: (selectedPatient.allergies || []).join(', '),
    })
  }, [selectedPatient, currentRole, currentUserId, patientsById])

  function savePatientProfile(event) {
    event.preventDefault()

    const targetId = patientEdit.userId
    const editingOwnPatient = currentRole === 'PATIENT' && currentUserId === targetId
    const allowed = editingOwnPatient || can('MANAGE_PATIENTS') || can('MANAGE_USERS')

    if (!allowed) {
      notify('error', 'You do not have permission to edit this profile.')
      return
    }

    const allergies = patientEdit.allergies
      .split(',')
      .map((a) => a.trim())
      .filter(Boolean)

    write((next) => {
      const row = next.patients.find((p) => p.userId === targetId)
      if (!row) throw new Error('Patient not found.')
      row.dob = patientEdit.dob || null
      row.insuranceId = patientEdit.insuranceId || null
      row.allergies = allergies
      addAudit(next, `UPDATE_PROFILE:${targetId}`, currentUserId)
    }, 'Patient profile updated.')
  }

  function scheduleAppointment(event) {
    event.preventDefault()
    if (!can('SCHEDULE_APPOINTMENT') && !can('MANAGE_USERS')) {
      notify('error', 'Only receptionist/admin can schedule appointments.')
      return
    }

    const patientId = appointmentForm.patientId.trim()
    const clinicianId = appointmentForm.clinicianId.trim()

    if (!patientsById.has(patientId)) {
      notify('error', 'Invalid patient ID.')
      return
    }

    const clinician = usersById.get(clinicianId)
    if (!clinician || clinician.role !== 'CLINICIAN') {
      notify('error', 'Invalid clinician ID.')
      return
    }

    if (!appointmentForm.scheduledAt) {
      notify('error', 'Please choose appointment datetime.')
      return
    }

    const appointmentId = uid()

    write((next) => {
      next.appointments.push({
        appointmentId,
        patientId,
        clinicianId,
        scheduledAt: new Date(appointmentForm.scheduledAt).toISOString(),
        status: 'PENDING',
      })
      addAudit(next, `SCHEDULE_APPOINTMENT:${appointmentId}`, currentUserId)
    }, 'Appointment scheduled.')

    setAppointmentForm({ patientId: '', clinicianId: '', scheduledAt: '' })
  }

  function cancelAppointment(appointmentId) {
    if (!can('CANCEL_APPOINTMENT') && !can('MANAGE_USERS')) {
      notify('error', 'Only receptionist, patient, or admin can cancel appointments.')
      return
    }

    write((next) => {
      const appt = next.appointments.find((a) => a.appointmentId === appointmentId)
      if (!appt) throw new Error('Appointment not found.')
      if (currentRole === 'PATIENT' && appt.patientId !== currentUserId) {
        throw new Error('Patients can cancel only their own appointments.')
      }
      if (appt.status === 'COMPLETED') {
        throw new Error('Completed appointments cannot be cancelled.')
      }
      appt.status = 'CANCELLED'
      addAudit(next, `CANCEL_APPOINTMENT:${appointmentId}`, currentUserId)
    }, 'Appointment cancelled.')
  }

  function createPrescription(event) {
    event.preventDefault()
    if (!can('CREATE_PRESCRIPTION') && !can('MANAGE_USERS')) {
      notify('error', 'Only clinician/admin can create prescriptions.')
      return
    }

    const appointmentId = rxForm.appointmentId.trim()
    const appointment = db.appointments.find((a) => a.appointmentId === appointmentId)

    if (!appointment) {
      notify('error', 'Appointment not found.')
      return
    }

    if (currentRole === 'CLINICIAN' && appointment.clinicianId !== currentUserId) {
      notify('error', 'Clinicians can create RX only for their appointments.')
      return
    }

    const rxId = uid()
    write((next) => {
      next.prescriptions.push({
        rxId,
        appointmentId,
        status: 'DRAFT',
        issuedAt: null,
        overrideReason: rxForm.overrideReason || null,
      })
      addAudit(next, `CREATE_PRESCRIPTION:${rxId}`, currentUserId)
    }, 'Prescription created.')

    setRxForm({ appointmentId: '', overrideReason: '' })
  }

  function transitionPrescription(rxId, action) {
    if (action === 'ISSUE' && !(can('ISSUE_PRESCRIPTION') || can('MANAGE_USERS'))) {
      notify('error', 'Only clinician/admin can issue prescriptions.')
      return
    }

    if (action === 'DISPENSE' && !(can('DISPENSE_PRESCRIPTION') || can('MANAGE_USERS'))) {
      notify('error', 'Only pharmacist/admin can dispense prescriptions.')
      return
    }

    if (action === 'VOID' && !(can('VOID_PRESCRIPTION') || can('MANAGE_USERS'))) {
      notify('error', 'Only clinician/admin can void prescriptions.')
      return
    }

    write((next) => {
      const rx = next.prescriptions.find((r) => r.rxId === rxId)
      if (!rx) throw new Error('Prescription not found.')

      const appt = next.appointments.find((a) => a.appointmentId === rx.appointmentId)
      if (!appt) throw new Error('Linked appointment missing.')

      if (currentRole === 'CLINICIAN' && appt.clinicianId !== currentUserId) {
        throw new Error('Clinicians can manage only their own prescriptions.')
      }

      if (action === 'ISSUE') {
        if (rx.status !== 'DRAFT') throw new Error('Can only issue DRAFT prescriptions.')
        rx.status = 'ISSUED'
        rx.issuedAt = nowStamp()
        appt.status = 'COMPLETED'
        addAudit(next, `ISSUE_PRESCRIPTION:${rxId}`, currentUserId)
        addAudit(next, `COMPLETE_APPOINTMENT:${appt.appointmentId}`, currentUserId)
        return
      }

      if (action === 'DISPENSE') {
        if (rx.status !== 'ISSUED') throw new Error('Can only dispense ISSUED prescriptions.')
        rx.status = 'DISPENSED'
        addAudit(next, `DISPENSE_PRESCRIPTION:${rxId}`, currentUserId)
        return
      }

      if (action === 'VOID') {
        if (rx.status === 'DISPENSED') throw new Error('Cannot void DISPENSED prescriptions.')
        rx.status = 'VOID'
        addAudit(next, `VOID_PRESCRIPTION:${rxId}`, currentUserId)
      }
    }, `Prescription ${action.toLowerCase()}d.`)
  }

  const patientRows = db.patients.map((p) => {
    const user = usersById.get(p.userId)
    return {
      ...p,
      email: user?.email || 'unknown',
    }
  })

  const appointmentRows = visibleAppointments.map((a) => ({
    ...a,
    patientEmail: usersById.get(a.patientId)?.email || 'unknown',
    clinicianEmail: usersById.get(a.clinicianId)?.email || 'unknown',
  }))

  const rxRows = visiblePrescriptions.map((r) => ({
    ...r,
    appointment: db.appointments.find((a) => a.appointmentId === r.appointmentId) || null,
  }))

  const ownPatient = currentRole === 'PATIENT' ? patientsById.get(currentUserId) : null

  const visibleAudit = useMemo(() => {
    if (currentRole === 'CLINIC_ADMIN') return db.audit
    if (currentUserId) return db.audit.filter((a) => a.userId === currentUserId)
    return []
  }, [db.audit, currentRole, currentUserId])

  return (
    <div className="scene">
      <div className="blob blob-a" />
      <div className="blob blob-b" />
      <div className="blob blob-c" />
      <div className="grain" />
      <div className="page">
      <header className="masthead">
        <div>
          <p className="eyebrow">Care Platform</p>
          <h1>HealthCare</h1>
          <p>Secure clinical workspace for appointments, prescriptions, and patient records.</p>
        </div>
        <div className="session-card">
          <p>Current User</p>
          {currentUser ? (
            <>
              <strong>{currentUser.email}</strong>
              <button type="button" onClick={logout}>Logout</button>
            </>
          ) : (
            <>
              <strong>Not logged in</strong>
              <small className="hint">Use sample credentials below.</small>
            </>
          )}
        </div>
      </header>

      <section className="panel compact">
        <strong>Demo Credentials:</strong>{' '}
        <span>admin@clinic.com / admin123</span>{' | '}
        <span>reception@clinic.com / recep123</span>{' | '}
        <span>clinician@clinic.com / doc123</span>{' | '}
        <span>pharmacist@clinic.com / pharma123</span>
      </section>

      <nav className="tabs">
        {visibleTabs.map((key) => (
          <button
            key={key}
            className={tab === key ? 'active' : ''}
            onClick={() => setTab(key)}
            type="button"
          >
            {TAB_LABELS[key]}
          </button>
        ))}
      </nav>

      <div className={`notice ${notice.type}`}>{notice.text}</div>

      {tab === 'auth' && (
        <section className="grid auth-stack">
          <article className="panel">
            <h2>Register Patient</h2>
            <form onSubmit={registerPatient} className="form-grid">
              <label>
                Email
                <input
                  value={registerForm.email}
                  onChange={(e) => setRegisterForm((f) => ({ ...f, email: e.target.value }))}
                  type="email"
                  required
                />
              </label>
              <label>
                Password
                <input
                  value={registerForm.password}
                  onChange={(e) => setRegisterForm((f) => ({ ...f, password: e.target.value }))}
                  type="password"
                  required
                />
              </label>
              <label>
                DOB
                <input
                  value={registerForm.dob}
                  onChange={(e) => setRegisterForm((f) => ({ ...f, dob: e.target.value }))}
                  type="date"
                />
              </label>
              <label>
                Insurance ID
                <input
                  value={registerForm.insuranceId}
                  onChange={(e) => setRegisterForm((f) => ({ ...f, insuranceId: e.target.value }))}
                />
              </label>
              <label className="full">
                Allergies (comma-separated)
                <input
                  value={registerForm.allergies}
                  onChange={(e) => setRegisterForm((f) => ({ ...f, allergies: e.target.value }))}
                />
              </label>
              <button type="submit">Register</button>
            </form>
          </article>

          <article className="panel">
            <h2>Login</h2>
            <form onSubmit={login} className="form-grid">
              <label>
                Email
                <input
                  value={loginForm.email}
                  onChange={(e) => setLoginForm((f) => ({ ...f, email: e.target.value }))}
                  type="email"
                  required
                />
              </label>
              <label>
                Password
                <input
                  value={loginForm.password}
                  onChange={(e) => setLoginForm((f) => ({ ...f, password: e.target.value }))}
                  type="password"
                  required
                />
              </label>
              <button type="submit">Login</button>
            </form>
            <p className="hint">Sign in to continue into your personalized workspace.</p>
          </article>
        </section>
      )}

      {tab === 'profile' && currentRole === 'PATIENT' && (
        <section className="panel">
          <h2>My Patient Profile</h2>
          {ownPatient ? (
            <form onSubmit={savePatientProfile} className="form-grid top-gap">
              <label>
                Patient ID
                <input value={ownPatient.userId} readOnly />
              </label>
              <label>
                DOB
                <input
                  type="date"
                  value={patientEdit.dob}
                  onChange={(e) => setPatientEdit((p) => ({ ...p, dob: e.target.value }))}
                />
              </label>
              <label>
                Insurance ID
                <input
                  value={patientEdit.insuranceId}
                  onChange={(e) => setPatientEdit((p) => ({ ...p, insuranceId: e.target.value }))}
                />
              </label>
              <label className="full">
                Allergies
                <input
                  value={patientEdit.allergies}
                  onChange={(e) => setPatientEdit((p) => ({ ...p, allergies: e.target.value }))}
                />
              </label>
              <button type="submit">Save My Profile</button>
            </form>
          ) : (
            <p className="hint">No patient profile is mapped to this user.</p>
          )}
        </section>
      )}

      {tab === 'users' && currentRole === 'CLINIC_ADMIN' && (
        <section className="grid two">
          <article className="panel">
            <h2>Create User by Role</h2>
            <form onSubmit={createUserByRole} className="form-grid">
              <label>
                Role
                <select
                  value={userCreateForm.role}
                  onChange={(e) => setUserCreateForm((f) => ({ ...f, role: e.target.value }))}
                >
                  <option value="PATIENT">Patient</option>
                  <option value="RECEPTIONIST">Receptionist</option>
                  <option value="CLINICIAN">Clinician</option>
                  <option value="PHARMACIST">Pharmacist</option>
                  <option value="CLINIC_ADMIN">Admin</option>
                </select>
              </label>
              <label>
                Email
                <input
                  value={userCreateForm.email}
                  onChange={(e) => setUserCreateForm((f) => ({ ...f, email: e.target.value }))}
                  type="email"
                  required
                />
              </label>
              <label>
                Password
                <input
                  value={userCreateForm.password}
                  onChange={(e) => setUserCreateForm((f) => ({ ...f, password: e.target.value }))}
                  type="password"
                  required
                />
              </label>

              {userCreateForm.role === 'CLINICIAN' && (
                <>
                  <label>
                    Licence No
                    <input
                      value={userCreateForm.licenceNo}
                      onChange={(e) =>
                        setUserCreateForm((f) => ({ ...f, licenceNo: e.target.value }))
                      }
                    />
                  </label>
                  <label>
                    Speciality
                    <input
                      value={userCreateForm.speciality}
                      onChange={(e) =>
                        setUserCreateForm((f) => ({ ...f, speciality: e.target.value }))
                      }
                    />
                  </label>
                </>
              )}

              {userCreateForm.role === 'RECEPTIONIST' && (
                <>
                  <label>
                    Staff ID
                    <input
                      value={userCreateForm.staffId}
                      onChange={(e) =>
                        setUserCreateForm((f) => ({ ...f, staffId: e.target.value }))
                      }
                    />
                  </label>
                  <label>
                    Shift
                    <input
                      value={userCreateForm.shift}
                      onChange={(e) => setUserCreateForm((f) => ({ ...f, shift: e.target.value }))}
                    />
                  </label>
                </>
              )}

              {userCreateForm.role === 'PHARMACIST' && (
                <>
                  <label>
                    Registration No
                    <input
                      value={userCreateForm.regNo}
                      onChange={(e) => setUserCreateForm((f) => ({ ...f, regNo: e.target.value }))}
                    />
                  </label>
                  <label>
                    Pharmacy
                    <input
                      value={userCreateForm.pharmacy}
                      onChange={(e) =>
                        setUserCreateForm((f) => ({ ...f, pharmacy: e.target.value }))
                      }
                    />
                  </label>
                </>
              )}

              {userCreateForm.role === 'CLINIC_ADMIN' && (
                <label className="full">
                  Admin Level
                  <input
                    value={userCreateForm.adminLevel}
                    onChange={(e) =>
                      setUserCreateForm((f) => ({ ...f, adminLevel: e.target.value }))
                    }
                  />
                </label>
              )}

              <button type="submit">Create User</button>
            </form>
          </article>

          <article className="panel">
            <h2>All Users ({db.users.length})</h2>
            <div className="scroll-table">
              <table>
                <thead>
                  <tr>
                    <th>Email</th>
                    <th>Role</th>
                    <th>User ID</th>
                  </tr>
                </thead>
                <tbody>
                  {db.users.map((u) => (
                    <tr key={u.userId}>
                      <td>{u.email}</td>
                      <td>{roleBadge(u.role)}</td>
                      <td>
                        <strong>{shortId(u.userId)}</strong>
                        <small>{u.userId}</small>
                        <button type="button" onClick={() => copyId('User ID', u.userId)}>
                          Copy ID
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </article>
        </section>
      )}

      {tab === 'patients' && (currentRole === 'RECEPTIONIST' || currentRole === 'CLINIC_ADMIN') && (
        <section className="grid two">
          <article className="panel">
            <h2>Find + Update Patient</h2>
            <label>
              Select Patient
              <select
                value={selectedPatientId}
                onChange={(e) => setSelectedPatientId(e.target.value)}
              >
                <option value="">Choose patient...</option>
                {patientOptions.map((p) => (
                  <option key={p.userId} value={p.userId}>
                    {p.email} ({shortId(p.userId)})
                  </option>
                ))}
              </select>
            </label>

            {selectedPatient ? (
              <form onSubmit={savePatientProfile} className="form-grid top-gap">
                <label>
                  Patient ID
                  <input value={patientEdit.userId} readOnly />
                </label>
                <label>
                  DOB
                  <input
                    type="date"
                    value={patientEdit.dob}
                    onChange={(e) => setPatientEdit((p) => ({ ...p, dob: e.target.value }))}
                  />
                </label>
                <label>
                  Insurance ID
                  <input
                    value={patientEdit.insuranceId}
                    onChange={(e) => setPatientEdit((p) => ({ ...p, insuranceId: e.target.value }))}
                  />
                </label>
                <label className="full">
                  Allergies
                  <input
                    value={patientEdit.allergies}
                    onChange={(e) => setPatientEdit((p) => ({ ...p, allergies: e.target.value }))}
                  />
                </label>
                <button type="submit">Save Profile</button>
              </form>
            ) : (
              <p className="hint top-gap">Search a patient to edit details.</p>
            )}
          </article>

          <article className="panel">
            <h2>Registered Patients ({patientRows.length})</h2>
            <div className="scroll-table">
              <table>
                <thead>
                  <tr>
                    <th>Email</th>
                    <th>Patient ID</th>
                    <th>DOB</th>
                    <th>Insurance</th>
                    <th>Allergies</th>
                  </tr>
                </thead>
                <tbody>
                  {patientRows.map((p) => (
                    <tr key={p.userId}>
                      <td>
                        <strong>{p.email}</strong>
                      </td>
                      <td>
                        <strong>{shortId(p.userId)}</strong>
                        <small>{p.userId}</small>
                        <button type="button" onClick={() => copyId('Patient ID', p.userId)}>
                          Copy ID
                        </button>
                      </td>
                      <td>{p.dob || '-'}</td>
                      <td>{p.insuranceId || '-'}</td>
                      <td>{(p.allergies || []).join(', ') || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </article>
        </section>
      )}

      {tab === 'appointments' && (
        <section className="panel">
          {(currentRole === 'RECEPTIONIST' || currentRole === 'CLINIC_ADMIN') && (
            <>
              <h2>Schedule Appointment</h2>
              <form onSubmit={scheduleAppointment} className="form-grid">
                <label>
                  Patient
                  <select
                    value={appointmentForm.patientId}
                    onChange={(e) =>
                      setAppointmentForm((f) => ({ ...f, patientId: e.target.value }))
                    }
                    required
                  >
                    <option value="">Choose patient...</option>
                    {patientOptions.map((p) => (
                      <option key={p.userId} value={p.userId}>
                        {p.email} ({shortId(p.userId)})
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Clinician
                  <select
                    value={appointmentForm.clinicianId}
                    onChange={(e) =>
                      setAppointmentForm((f) => ({ ...f, clinicianId: e.target.value }))
                    }
                    required
                  >
                    <option value="">Choose clinician...</option>
                    {clinicians.map((c) => (
                      <option key={c.userId} value={c.userId}>
                        {c.email} ({shortId(c.userId)})
                      </option>
                    ))}
                  </select>
                </label>
                <label className="full">
                  Scheduled At
                  <input
                    value={appointmentForm.scheduledAt}
                    onChange={(e) =>
                      setAppointmentForm((f) => ({ ...f, scheduledAt: e.target.value }))
                    }
                    type="datetime-local"
                    required
                  />
                </label>
                <button type="submit">Schedule</button>
              </form>
            </>
          )}

          <h3>Visible Appointments ({appointmentRows.length})</h3>
          <div className="appointment-list">
            {appointmentRows.map((a) => (
              <div className="appointment-card" key={a.appointmentId}>
                <p>
                  <strong>{shortId(a.appointmentId)}</strong>
                  <span className={`status ${a.status.toLowerCase()}`}>{a.status}</span>
                </p>
                <small>
                  Appointment ID: {a.appointmentId}
                  <button
                    type="button"
                    onClick={() => copyId('Appointment ID', a.appointmentId)}
                  >
                    Copy ID
                  </button>
                </small>
                <small>
                  {a.patientEmail} {'->'} {a.clinicianEmail}
                </small>
                <small>{new Date(a.scheduledAt).toLocaleString()}</small>
                {(currentRole === 'RECEPTIONIST' || currentRole === 'CLINIC_ADMIN' || currentRole === 'PATIENT') && (
                  <button
                    type="button"
                    disabled={a.status === 'CANCELLED' || a.status === 'COMPLETED'}
                    onClick={() => cancelAppointment(a.appointmentId)}
                  >
                    Cancel
                  </button>
                )}
              </div>
            ))}
          </div>
        </section>
      )}

      {tab === 'prescriptions' && (
        <section className="grid two">
          {(currentRole === 'CLINICIAN' || currentRole === 'CLINIC_ADMIN') && (
            <article className="panel">
              <h2>Create Prescription</h2>
              <form onSubmit={createPrescription} className="form-grid">
                <label>
                  Appointment
                  <select
                    value={rxForm.appointmentId}
                    onChange={(e) => setRxForm((f) => ({ ...f, appointmentId: e.target.value }))}
                    required
                  >
                    <option value="">Choose appointment...</option>
                    {rxAppointmentOptions.map((a) => (
                      <option key={a.appointmentId} value={a.appointmentId}>
                        {shortId(a.appointmentId)} - {usersById.get(a.patientId)?.email || 'unknown'}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="full">
                  Override Reason (optional)
                  <input
                    value={rxForm.overrideReason}
                    onChange={(e) => setRxForm((f) => ({ ...f, overrideReason: e.target.value }))}
                  />
                </label>
                <button type="submit">Create RX</button>
              </form>
            </article>
          )}

          <article className="panel">
            <h2>Visible Prescriptions ({rxRows.length})</h2>
            <div className="appointment-list">
              {rxRows.map((r) => (
                <div className="appointment-card" key={r.rxId}>
                  <p>
                    <strong>{shortId(r.rxId)}</strong>
                    <span className={`status ${r.status.toLowerCase()}`}>{r.status}</span>
                  </p>
                  <small>
                    Prescription ID: {r.rxId}
                    <button type="button" onClick={() => copyId('Prescription ID', r.rxId)}>
                      Copy ID
                    </button>
                  </small>
                  <small>
                    Appointment: {r.appointmentId}
                    <button
                      type="button"
                      onClick={() => copyId('Appointment ID', r.appointmentId)}
                    >
                      Copy ID
                    </button>
                  </small>
                  <small>Issued At: {r.issuedAt ? new Date(r.issuedAt).toLocaleString() : '-'}</small>
                  {r.overrideReason ? <small>Override: {r.overrideReason}</small> : null}
                  <div className="row-actions">
                    {(currentRole === 'CLINICIAN' || currentRole === 'CLINIC_ADMIN') && (
                      <button
                        type="button"
                        disabled={r.status !== 'DRAFT'}
                        onClick={() => transitionPrescription(r.rxId, 'ISSUE')}
                      >
                        Issue
                      </button>
                    )}
                    {(currentRole === 'PHARMACIST' || currentRole === 'CLINIC_ADMIN') && (
                      <button
                        type="button"
                        disabled={r.status !== 'ISSUED'}
                        onClick={() => transitionPrescription(r.rxId, 'DISPENSE')}
                      >
                        Dispense
                      </button>
                    )}
                    {(currentRole === 'CLINICIAN' || currentRole === 'CLINIC_ADMIN') && (
                      <button
                        type="button"
                        disabled={r.status === 'DISPENSED' || r.status === 'VOID'}
                        onClick={() => transitionPrescription(r.rxId, 'VOID')}
                      >
                        Void
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </article>
        </section>
      )}

      {tab === 'audit' && currentRole === 'CLINIC_ADMIN' && (
        <section className="panel">
          <h2>Audit Log ({visibleAudit.length})</h2>
          <div className="scroll-table">
            <table>
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Action</th>
                  <th>User</th>
                </tr>
              </thead>
              <tbody>
                {visibleAudit.map((a) => {
                  const u = a.userId ? usersById.get(a.userId) : null
                  return (
                    <tr key={a.id}>
                      <td>{new Date(a.timestamp).toLocaleString()}</td>
                      <td>{a.action}</td>
                      <td>{u ? `${u.email} (${a.userId.slice(0, 8)}...)` : '-'}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </section>
      )}

      <footer className="footnote">
        Role-based visibility is enforced in both navigation and action handlers.
      </footer>
      </div>
    </div>
  )
}

export default App
